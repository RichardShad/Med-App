(ns frontend.patient.views
  (:require [frontend.routes :as routes]
            [frontend.patient.subs :as subs]
            [re-frame.core :as re-frame]
            [re-com.core :refer [v-box box button h-box border]]))

(defn patient-view [patient]
  [v-box :children [(:name patient)]])

(defn patient-panel []
  (let [id @(re-frame/subscribe [::subs/patient-id])
        patient @(re-frame/subscribe [::subs/patient id])]
    [v-box :children [[:h1 (str "Patient #" id)]
                      [patient-view patient]]]))

(defmethod routes/panels :patient-panel [] [patient-panel])