(ns medui.patient.views
  (:require [re-frame.core :as re-frame]
            [medui.events :as core-events]
            [medui.subs :as core-subs]
            [medui.components :as c]
            [medui.routes :as routes]
            [medui.patient.events :as events]
            [medui.patient.subs :as subs]))

(defn patient-view [patient]
  [:div 
   (c/view-pass-argument :div patient
                         [c/patient-name-view [:b "Name: "]]
                         (when (:gender patient)
                           [c/patient-gender-view [:b "Gender: "]])
                         (when (:dob patient)
                           [c/patient-dob-view [:b "Date of birth: "]])
                         (when (:oms-number patient)
                           [c/patient-oms-num-view [:b "CMI №: "]])
                         (when (:address patient)
                           [c/patient-address-view [:b "Address: "]]))])

(defn patient-update-view [patient]
  [:div
   [c/patient-input patient]
   [:div
    [c/patient-sumbit [::events/update-patient (:id patient)]]]])

(defn patient-delete-view [patient]
  [:div [c/operation-view
         [c/patient-name-view "Are you sure you want to delete the pacient: " patient]
         [c/patient-name-view "Deleteion failed for the patient: "]]
   [c/button "Confirm" #(re-frame/dispatch [::events/delete-patient (:id patient)])]])

;;[operation-choose-view op patient] :read [patient-view]

(def operation-choose-view 
  (c/make-choice-view {:delete [patient-delete-view]
                       :update [patient-update-view]
                       :default [patient-view]}))

(def option-panel (c/make-option-panel [:update :delete] operation-choose-view))

(defn patient-panel [id]
  (let [op @(re-frame/subscribe [::core-subs/operation])
        patient @(re-frame/subscribe [::subs/patient])]
    [:div [:h1 (str "Patient #" id " " (name op))]
     [:div ((comp c/loading-view c/loaded-view)
            [option-panel patient])]
     [c/refresh-button [::events/fetch-patient id]]]))

(defmethod routes/panels :patient-panel [{:keys [params]}]
  [patient-panel (:id params)])

(defmethod routes/effects-for-path :patient [{:keys [route-params]}] 
  (re-frame/dispatch [::events/fetch-patient (:id route-params)]))
