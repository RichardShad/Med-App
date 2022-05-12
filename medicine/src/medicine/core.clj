(ns medicine.core
  (:require [ring.adapter.jetty :as jetty]
            [medicine.db :as db]
            [medicine.routing :as routing]
            [medicine.handler :as handler]))

(def db-spec {:dbtype "postgres"
              :dbname "medb"
              :user "medadmin"
              :password "123123"})

(def ds (db/get-datasource db-spec))

(def handler
  (handler/make-handler ds))

(def app (routing/make-router handler))

(comment

  (app {:request-method :get :uri "/api/v1/patient/id/10"})

  (db/get-patient 13 ds)
  (db/delete-patient! 8 ds)

  (import '[java.sql Date])
  (db/create-patient! {:first_name "George"
                       :last_name "Dianov"
                       :second_name "Alexeevich"
                       :gender "male"
                       :dob (. Date valueOf "2002-06-10")
                       :oms_number 123532451
                       :address "Ul. Korp!"}
                      ds)
  
)

(defn -main
  [& _]
  (jetty/run-jetty app {:port 3000}))
