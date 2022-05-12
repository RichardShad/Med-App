(ns backend.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [backend.handler :as h]
            [backend.db :refer [ds]]
            [backend.conf :refer [port]]))

(def app (h/make-handler ds))

(defn -main
  [& _]
  (jetty/run-jetty app {:port port}))
