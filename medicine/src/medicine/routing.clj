(ns medicine.routing
  (:require [reitit.core :as r]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]))

(defn- wrap-operation
  [handler operation]
  (fn [request]
    (handler (assoc request :operation operation))))

(defn- wrap-all?
  [handler all?]
  (fn [request]
    (handler (assoc request :all all?))))

(defn make-router [handler]
  (ring/ring-handler
   (ring/router ["/api/v1" {:get {:middleware [[wrap-operation :read]]}
                            :delete {:middleware [[wrap-operation :delete]]}}

                 ["/patients" {:name ::patients
                               :handler handler
                               :middleware [[wrap-all? true]]
                               :post {:middleware [[wrap-operation :create]]}
                               :put {:middleware [[wrap-all? false]
                                                  [wrap-operation :create]]}
                               :options {:handler
                                         (fn [_] {:status 200
                                                  :headers {"Allow" "GET,POST,PUT,DELETE"
                                                            "Access-Control-Allow-Methods" "GET,POST,PUT,DELETE"}})}}]

                 ["/patient/id/{id}" {:name ::patient
                                      :middleware [[wrap-all? false]]
                                      :handler handler
                                      :parameters {:path
                                                   {:id nat-int?}}
                                      :patch {:middleware
                                              [[wrap-operation :update]]}
                                      :options {:handler
                                                (fn [_] {:status 200
                                                         :headers {"Allow" "GET,PATCH,DELETE"
                                                                   "Access-Control-Allow-Methods" "GET,PATCH,DELETE"}})}}]]

                {:data {:muuntaja m/instance
                        :coercion reitit.coercion.spec/coercion
                        :middleware [muuntaja/format-middleware
                                     coercion/coerce-exceptions-middleware
                                     coercion/coerce-request-middleware
                                     (fn [handler]
                                       (fn [request]
                                         (assoc-in (handler request) 
                                                   [:headers "Access-Control-Allow-Origin"] "*")))]}})))

(defn patient-uri [{::r/keys [router]} id]
  (-> router
      (r/match-by-name ::patient {:id id})
      (r/match->path)))