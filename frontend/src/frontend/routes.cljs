(ns frontend.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [frontend.events :as events]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes
  (atom
    ["/" {""      :home
          "patients" {"" :patients
                      ["/" :id] :patient}}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [panel (keyword (str (name (:handler route)) "-panel"))
        params (:route-params route)]
    (re-frame/dispatch [::events/set-active-panel {:name panel
                                                   :panel (panels panel)
                                                   :params params}])))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [handler args]
  (pushy/set-token! history (apply url-for handler args)))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler & args]
    (apply navigate! handler args)))
