(ns medui.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame]
   [medui.events :as events]
   [medui.db :as db]))

(defmulti panels :name)
(defmethod panels :default [] [:div "No panel found for this route."])

(defmulti effects-for-path :handler)
(defmethod effects-for-path :default [] nil)

(def routes
  (atom
    ["/" {""      :home
          "about" :about
          "patients" {"" :patient-index
                      ["/" :id] :patient}}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn dispatch
  [route]
  (let [name (keyword (str (name (:handler route)) "-panel"))
        params (:route-params route)
        page {:name name
              :params params
              :panel (panels {:name name :params params})}]
    (re-frame/dispatch [::events/set-active-page (merge page db/default-page-state)])
    (effects-for-path route)))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [handler]
  (pushy/set-token! history (if (coll? handler)
                              (apply url-for handler)
                              (url-for handler))))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
  :navigate
  (fn [handler]
    (navigate! handler)))
