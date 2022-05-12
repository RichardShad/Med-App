(ns medui.patients.views
  (:require [re-frame.core :as re-frame]
            [medui.components :as c]
            [medui.routes :as routes]
            [medui.subs :as core-subs]
            [medui.events :as core-events]
            [medui.patients.subs :as subs]
            [medui.patients.events :as events]))

(defn patient-view [patient]
  [:li {:key (:id patient)}
   [:a
    {:on-click #(re-frame/dispatch [::core-events/navigate [:patient :id (:id patient)]])}
    (c/get-patient-name patient)]])

(defn patients-view [patients]
  [:div [(comp c/loading-view c/loaded-view)
               [into [:ul.index] (mapv patient-view patients)]]])

(defn patient-create-view [_]
  [:div
   [c/patient-input]
   [c/make-input-submit [::events/create-patient] c/patient-fields]])

(defn patients-delete-view [_]
  [:div
   [:p "Are you sure you want to delete all the patients?"]
   [c/button "Confirm" #(re-frame/dispatch [::events/delete-patients])]])

(def operation-choose-view
  (c/make-choice-view {:delete [patients-delete-view]
                       :create [patient-create-view]
                       :default [patients-view]}))

(def option-panel (c/make-option-panel [:create :delete] operation-choose-view))

(defn patient-index-panel []
  (let [op @(re-frame/subscribe [::core-subs/operation])
        patients @(re-frame/subscribe [::subs/patients])]
    [:div [:h1 (str "Patient index: " (name op))]
     [:div ((comp c/loading-view c/loaded-view)
            [option-panel patients])]
     [c/refresh-button [::events/fetch-patients]]]))

(defmethod routes/panels :patient-index-panel [] [patient-index-panel])
(defmethod routes/effects-for-path :patient-index [] 
  (re-frame/dispatch [::events/fetch-patients]))