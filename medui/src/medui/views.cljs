(ns medui.views
  (:require
   [re-frame.core :as re-frame]
   [medui.events :as events]
   [medui.routes :as routes]
   [medui.subs :as subs]
   [medui.patients.views]
   [medui.patient.views]))


;; home

(defn home-panel []
  [:div
   [:h1
    (str "Home page")]])

(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-panel []
  [:div
   [:h1 "About Page."]
   [:p "This is an UI application for simple operating medicine DB."]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn nav-link-veiw [idx [k name]]
  [:li {:key idx} [:a {:on-click #(re-frame/dispatch [::events/navigate k])}
                   name]])

(defn nav-bar-view [pages]
  [:nav [:ul.nav-links (map-indexed nav-link-veiw pages)]])

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [::subs/active-panel])]
    [:div [:header [nav-bar-view [[:home "Home"] [:patient-index "Patients"] [:about "About"]]]]
     active-panel]))

;;active-panel