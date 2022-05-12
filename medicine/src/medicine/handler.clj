(ns medicine.handler
  (:require [medicine.db :as db]
            [medicine.routing :as routing]
            [clojure.string :as str]
            [ring.util.response :as response])
  (:import [java.sql Date]))

(defn- delete-status-fn [res]
  (if (<= (:next.jdbc/update-count res) 0)
    404
    204))

(defn- zip [& vs]
  (apply (partial map vector) vs))

(defn- map-translate [dict m]
  (let [ks (keys dict)
        ks-getter (apply juxt ks)
        m-values (ks-getter m)
        new-ks (ks-getter dict)]
    
    (->> (zip new-ks m-values)
         (filter (fn [[_ v]] v))
         (flatten)
         (apply hash-map)
         (merge (apply dissoc m ks)))))

(def patient-translation
  (transduce (map (fn [key]
                    [key
                     (-> key
                         (name)
                         (str/replace #"-" "_")
                         (keyword))]))
             (completing (fn [m [k v]]
                           (assoc m k v)))
             {} [:first-name :second-name :last-name :oms-number
                 :gender :address :dob :id]))

(def patient-translation-inv
  (into {}
        (for [[k v] patient-translation]
          [(->> v (name) (keyword "patient")) k])))

(def handler-structure {:patients
                        {:read {:operation db/get-patients
                                :args [:db]
                                :after-fn (comp
                                           #(assoc {} :patients %)
                                           #(map
                                             (partial map-translate patient-translation-inv)
                                             %))
                                :status 200}

                         :create {:operation db/create-patients!
                                  :args [:patients :db]
                                  :status 200
                                  :result "Created"}

                         :delete {:operation db/empty-table!
                                  :args [:db]
                                  :result nil
                                  :status-fn delete-status-fn}}

                        :patient
                        {:read {:operation db/get-patient
                                :args [:id :db]
                                :after-fn (comp
                                           #(when (seq %) (assoc {} :patient %))
                                           (partial map-translate patient-translation-inv))
                                :status-fn (fn [res]
                                             (if (empty? res)
                                               404
                                               200))}

                         :create {:operation db/create-patient!
                                  :args [:patient :db]
                                  :headers-fn (fn [request]
                                                {"Location" (routing/patient-uri request
                                                                                 (:op-res request))})
                                  :status 201
                                  :result "Patient created"}

                         :delete {:operation db/delete-patient!
                                  :args [:id :db]
                                  :status-fn delete-status-fn
                                  :result nil}

                         :update {:operation db/update-patient!
                                  :args [:id :patient :db]
                                  :status 200
                                  :result "Patient data updated"}}})

(defn- up-handler-keys [request ks]
  (transduce (comp
              (map (fn [ks]
                     [(last ks) (get-in request ks)]))
              (filter second))
             (completing (fn [m [k v]]
                           (assoc m k v)))
             {} ks))

(defn- set-status [response status]
  (assoc response :status status))

(defn- patient->db [data]
  (-> data
      (update :dob #(. Date valueOf %))
      (map-translate patient-translation)))

(defn- patients->db [data]
  (map patient->db data))

(defn- data->db [data]
  (cond 
    (contains? data :patient) (update data :patient patient->db)
    (contains? data :patients) (update data :patients patients->db)
    :else data))

(defn- set-headers [response headers]
  (assoc response :headers headers))

(defn- who? [request] 
  (assoc request :who
         (if (:all request)
           :patients
           :patient)))

(defn- select-structure
  [request structure path]
  (assoc request :structure
         (loop [structure structure
                path path]
           (when structure
             (if path
               (recur
                (-> path
                    (first)
                    (->> (get request))
                    (->> (get structure)))
                (not-empty (rest path)))
               structure)))))

(defn- prepare-data [request db]
  (let [who (:who request)]
    (assoc request :data
           (-> request
               (up-handler-keys
                [[:parameters :path :id] [:body who]])
               (data->db)
               (assoc :db db)))))

(defn- prepare-args [request]
  (assoc request :args
         (let [arg-list (get-in request [:structure :args])]
           (if (pos? (count arg-list))
             ((apply juxt arg-list)
                    (:data request))
             []))))

(defn- proceed-operation [request]
         (when-let [operation (get-in request [:structure :operation])]
           (-> request
               (:args)
               (->> (apply operation))
               (->> (assoc request :op-res)))))

(defn- apply-after [request]
  (if-let [after-fn (get-in request [:structure :after-fn])]
    (update request :op-res after-fn)
    request))

(defn- get-result [request]
  (assoc request :result
         (if (contains? (:structure request) :result)
           (get-in request [:structure :result])
           (:op-res request))))

(defn- set-result-as-body [request]
  (assoc request :body (:result request)))

(defn- set-structure-status [request]
  (let [status (get-in request [:structure :status])
        status-fn (get-in request [:structure :status-fn])
        op-res (:op-res request)]
    (set-status request (or status
                            (status-fn op-res)))))

(defn- get-headers [request]
  (let [structutre (:structure request)]
    (set-headers request
           (or (:headers structutre)
               (when-let [headers-fn (:headers-fn structutre)]
                 (headers-fn request))
               {}))))

(defn- prepare-to-operation [request db]
  (-> request
      (who?)
      (select-structure handler-structure [:who :operation])
      (prepare-data db)
      (prepare-args)))

(defn- make-response [request]
  (-> request
      (apply-after)
      (get-result)
      (set-structure-status)
      (set-result-as-body)
      (get-headers)))

(defn- proceed-request [request db]
  (-> request
      (prepare-to-operation db)
      (proceed-operation)
      (make-response)
      (select-keys [:body :status :headers])))

(defn make-handler [db]
  (fn [request]
    (println (.toString (:body request)))
    (if (and (contains? request :operation)
             (contains? request :all)
             db)
      (proceed-request request db)
      (response/bad-request "Operation is not available."))))