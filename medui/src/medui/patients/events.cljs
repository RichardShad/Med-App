(ns medui.patients.events
  (:require [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [medui.helpers :refer [endpoint prepare-patient-data]]))

(re-frame/reg-event-fx
 ::fetch-patients
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:active-page :loading] true)
    :http-xhrio {:method :get
                 :uri (endpoint "patients")
                 :timeout 8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::fetch-patients-success]
                 :on-failure [::fetch-patients-feilure]}}))

(re-frame/reg-event-fx
 ::create-patient
 (fn [{:keys [db]} [_ patient-data]]
   {:db (assoc-in db [:active-page :loading] true)
    :http-xhrio {:method :put
                 :uri (endpoint "patients")
                 :timeout 8000
                 :params {:patient (prepare-patient-data patient-data)}
                 :request-format (ajax/json-request-format)
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::create-patient-success]
                 :on-failure [::create-patient-feilure]}}))

(re-frame/reg-event-fx
 ::delete-patients
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:active-page :loading] true)
    :http-xhrio {:method :delete
                 :uri (endpoint "patients")
                 :timeout 8000
                 :request-format :raw
                 :body {}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::delete-patients-success]
                 :on-failure [::delete-patients-feilure]}}))

(re-frame/reg-event-db
 ::fetch-patients-success
 (fn [db [_ {:keys [patients]}]]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :patients patients)
                              (assoc :loaded true)
                              (assoc :loading false)))))

(re-frame/reg-event-db
 ::fetch-patients-feilure
 (fn [db _]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :loaded false)
                              (assoc :loading false)))))

(re-frame/reg-event-fx
 ::create-patient-success
 (fn [{:keys [db]} _]
   {:db (assoc db :active-page (-> (:active-page db)
                                   (assoc :loading false)
                                   (assoc :op-success true)
                                   (assoc :operation :read)))
    :fx [[:dispatch [::fetch-patients]]]}))

(re-frame/reg-event-db
 ::create-patient-feilure
 (fn [db _]
   (assoc db :active-page (-> (:active-page db)
                              (assoc :loading false)
                              (assoc :op-success false)))))

(re-frame/reg-event-fx 
 ::delete-patient-success
 (fn [{:keys [db]} _]
   {:db (assoc db :active-page (-> (:active-page db)
                                   (assoc :loading false)
                                   (assoc :op-success true)
                                   (assoc :operation :read)))
    :fx [[:dispatch [::fetch-patients]]]}))
