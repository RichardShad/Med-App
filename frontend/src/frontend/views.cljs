(ns frontend.views
  (:require
   [frontend.patients.views]
   [re-frame.core :as re-frame]
   [frontend.events :as events]
   [frontend.routes :as routes]
   [frontend.subs :as subs]
   [re-com.core :refer [h-box v-box button box]]))


;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      (str "Hello from " @name ". This is the Home Page.")]

     [:div
      [:a {:on-click #(re-frame/dispatch [::events/navigate :about])}
       "go to About Page"]]
     ]))

(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(re-frame/dispatch [::events/navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [::subs/active-panel])]
    [v-box
     :size "auto"
     :gap "10px"
     :children [[h-box
                 :gap "10px"
                 :children [[button :label "Home"
                             :on-click #(re-frame/dispatch [::events/navigate :home])]
                            [button :label "Patients"
                             :on-click #(re-frame/dispatch [::events/navigate :patients])]]]
                [h-box :gap "5px"
                 :children [active-panel]]]]))
