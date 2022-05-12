(ns medui.patient.events
  (:require [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [medui.helpers :refer [endpoint prepare-patient-data]]))

(re-frame/reg-event-fx
 ::fetch-patient
 (fn [{:keys [db]} [_ id]]
   {:db (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri (endpoint "patient" "id" id)
                 :timeout 8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::fetch-patient-success]
                 :on-failure [::fetch-patient-feilure]}}))

(re-frame/reg-event-fx
 ::update-patient
 (fn [{:keys [db]} [_ id patient]]
   {:db (assoc-in db [:active-page :loading] true)
    :http-xhrio {:method :patch
                 :uri (endpoint "patient" "id" id)
                 :timeout 8000
                 :params {:patient (prepare-patient-data patient)}
                 :format (ajax/json-request-format)
                 :request-format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::update-patient-success]
                 :on-failure [::update-patient-failure]}}))

(re-frame/reg-event-fx
 ::delete-patient
 (fn [{:keys [db]} [_ id]]
   {:db
    (assoc-in db [:active-page :loading] true)
    :http-xhrio {:method :delete
                 :uri (endpoint "patient" "id" id)
                 :timeout 8000
                 :request-format :raw
                 :body {}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::delete-patient-success]
                 :on-failure [::delete-patient-feilure]}}))

(re-frame/reg-event-db
 ::fetch-patient-success
 (fn [db [_ {:keys [patient]}]]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :patient patient)
                              (assoc :loading false)
                              (assoc :loaded true)))))

(re-frame/reg-event-db
 ::fetch-patient-feilure
 (fn [db _]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :loaded false)
                              (assoc :loading false)))))

(re-frame/reg-event-fx
 ::update-patient-success
 (fn [{:keys [db]} _]
   {:db (assoc db :active-page (-> (:active-page db)
                              (assoc :loading false)
                              (assoc :op-success true)
                              (assoc :operation :read)))
    :fx [[:dispatch [::fetch-patient (get-in db [:active-page :params :id])]]]}))

(re-frame/reg-event-db
 ::update-patient-failure
 (fn [db _]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :loading false)
                              (assoc :op-success false)))))

(re-frame/reg-event-fx
 ::delete-patient-success
 (fn [_ _]
   {:navigate :patient-index}))

(re-frame/reg-event-db
 ::delete-patient-feilure
 (fn [db _]
   (assoc db :active-page  (-> (:active-page db)
                               (assoc :loading false)
                               (assoc :op-success false)))))
