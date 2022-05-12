(ns frontend.patients.views
  (:require [clojure.string :refer [capitalize]]
            [frontend.routes :as routes]
            [frontend.patients.subs :as subs]
            [frontend.patients.events :as events]
            [re-frame.core :as re-frame]
            [re-com.core :refer [v-box box button h-box border]]))

(defn patient-view [patient]
  [border
   :style {:width "15%"}
   :child [h-box
           :align :stretch
           :size "auto"
           :children [[box 
                       :style {:user-select "none"} 
                       :size "auto" :child (:name patient)]
                      [button
                       :style {:object-position "right"}
                       :on-click #(js/alert (str "Patient #" (:id patient)))
                       :label "go"]]]])

(defn operation-buttons [cur op-lst]
  (if (= cur :read)
    [h-box :size "auto" (mapv (fn [op]
                                [button :label (capitalize (name op))
                                 :on-click #(re-frame/dispatch [::events/set-operation op])])
                              op-lst)]
    [box :size "auto" 
     [button :label "Back" :on-click #(re-frame/dispatch [::events/set-operation :read])]]))

(defn patients-view [patients]
  [v-box :size "auto" :children (mapv patient-view patients)])

(defn patient-create-view []
  )

(defn patients-delete-view [])

(defn patients-panel []
  (let [patients @(re-frame/subscribe [::subs/patients])
        operation @(re-frame/subscribe [::subs/operaion])
        op (or operation :read)]
    [v-box
     :size "auto"
     :gap "20px"
     :children [(case op
                  :read [patients-view patients]
                  :create [patient-create-view]
                  :delete [patients-delete-view]
                  [:p "error occured: incorrect operation specified"])
                [operation-buttons op [:create :delete]]
                [button
                 :label "Refresh"
                 :on-click #(js/alert "Refresh")]]]))

(defmethod routes/panels :patients-panel [] [patients-panel])
