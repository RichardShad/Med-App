(ns backend.handler
  (:require [backend.db :as db]
            [backend.conf :refer [client-url]]
            [reitit.core :as r]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.string :as str])

  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

;; helpers

(defn check [m k pred]
  (if (contains? m k)
    (do (println (str (pred (k m))))
        (pred (k m)))
    true))

(defn request-validator [request validator]
  (validator request))

(defn body-validator [request validator]
  (when (:body-params request)
    (request-validator request (comp validator :body-params))))

(defn name? [n]
  (and (string? n)
       (re-matches #"[А-ЯA-Z][а-яa-z]+" n)))

(defn gender? [g]
  (and (string? g)
       (#{"male" "female"} g)))

(defn patient? [p]
  (and (check p :first-name name?)
       (check p :second-name name?)
       (check p :last-name name?)
       (check p :gender gender?)
       (check p :oms-number int?)
       ;; (check p :dob string?) Can't validate date for some reason, everything else works fine
       (check p :address string?)))

(defn validate-patient? [request]
  (when (get-in request [:body-params :patient])
    (body-validator request (comp patient?
                                  :patient))))

(defn parse-date [dstr]
  (. LocalDate parse dstr (. DateTimeFormatter ISO_DATE_TIME)))

(defn filter-empties-out [m]
  (into {} (filter (comp some? val) m)))

(defn update-when-contains "Performs funtion f to the field k if the map m has the key k"
  [m k f]
  (if (and (map? m) (contains? m k))
    (update m k f)
    m))

(defn mapm "Map for maps"
  [f m]
  (apply hash-map (mapcat (fn [[k v]] (f k v)) m)))

(defn api-key->db-key [k]
  (keyword (str/replace (name k) #"-" "_")))

(defn db-key->api-key [k]
  (keyword (str/replace (name k) #"_" "-")))

(defn api-map->db-map [m]
  (-> m
      (->> (mapm (fn [k v] [(api-key->db-key k) v])))
      (update-when-contains :dob parse-date)))

(defn db-map->api-map [m]
  (mapm (fn [k v] [(db-key->api-key k) v]) m))

(defn translate-request-keys [req tr as-body]
  (if (as-body req)
    (assoc req as-body (-> (as-body req)
                           (update-when-contains :patient tr)
                           (update-when-contains :patients (partial map tr))))
    req))

(defn patient-uri "Returns uri for the patient with specified id"
  [request id]
  (let [{::r/keys [router]} request]
    (-> router
        (r/match-by-name ::patient {:id id})
        (r/match->path))))

(defn get-id "Gets id from request path parameters"
  [request]
  (get-in request [:parameters :path :id]))

;; middleware

(defn wrap-validator [handler validator]
  (fn [req]
    (if (validator req)
      (handler req)
      {:status 400})))

(defn wrap-request-translation [handler]
  (fn [request]
    (handler (translate-request-keys request api-map->db-map :body-params))))

(defn wrap-response-translation [handler]
  (fn [request]
    (let [result (handler request)]
      (translate-request-keys result db-map->api-map :body))))

(defn wrap-ds "Middleware wrapping datasource for database"
  [handler ds]
  (fn [request]
    (handler (assoc request :ds ds))))

;; handlers for one patient operations

(defn patient-read "Read handler for one patient"
  [request]
  (let [id (get-id request)
        ds (:ds request)
        patient (db/get-patient id ds)]
    {:headers {}
     :status (if patient 200 404)
     :body (if patient {:patient (filter-empties-out patient)} {})}))

(defn patient-update "Update handler for one patient"
  [request]
  (let [id (get-id request)
        patient (get-in request [:body-params :patient])
        ds (:ds request)]
    (db/update-patient! id patient ds)
    (if-let [patient (filter-empties-out (not-empty (db/get-patient id ds)))]
      {:headers {} :status 200 :body {:patient patient}}
      {:headers {} :status 404 :body {}})))

(defn patient-delete "Delete handler for one patient"
  [request]
  (let [id (get-id request)
        ds (:ds request)
        res (db/delete-patient! id ds)]
    {:headers {}
     :status (if (pos? (:next.jdbc/update-count res))
               204
               404)
     :body {}}))

(defn patient-create "Create handler for one patient"
  [request]
  (let [patient (get-in request [:body-params :patient])
        ds (:ds request)
        id (db/create-patient! patient ds)]
    {:headers {"location" (patient-uri request id)}
     :status 200
     :body {}}))

;; handlers for all patients

(defn patients-read "Read handler for the whole database"
  [request]
  (let [ds (:ds request)
        patients (map filter-empties-out (db/get-patients ds))]
    {:headres {} :status 200 :body {:patients patients}}))

(defn patients-delete "Delete handler for the whole database"
  [request]
  (let [ds (:ds request)]
    (db/empty-table! ds)))

;; main handler

(defn make-handler "Creates route handler for the api"
  [ds]
  (ring/ring-handler
   (ring/router ["/api/v1" ::api-root
                 ["/patients" {:name ::patients
                               :get patients-read
                               :delete patients-delete
                               :put {:handler patient-create
                                     :middleware [[wrap-validator validate-patient?]]}}]

                 ["/patient/id/{id}" {:name ::patient
                                      :parameters {:path {:id nat-int?}}
                                      :get patient-read
                                      :delete patient-delete
                                      :patch {:handler patient-update
                                              :middleware [[wrap-validator validate-patient?]]}}]]

                {:data {:muuntaja m/instance
                        :coercion reitit.coercion.spec/coercion
                        :middleware [[wrap-cors
                                      :access-control-allow-origin [(re-pattern client-url)]
                                      :access-control-allow-methods #{:get :patch :post :delete :put}]
                                     [wrap-ds ds]
                                     coercion/coerce-request-middleware
                                     coercion/coerce-exceptions-middleware
                                     muuntaja/format-middleware
                                     wrap-request-translation
                                     wrap-response-translation]}})
   (ring/create-default-handler)))
