(ns backend.db
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.date-time]
   [next.jdbc.types :refer [as-other]]
   [backend.conf :refer [db-name db-user db-password db-url]]))

(def db-spec {:dbtype "postgres"
              :dbname db-name
              :user db-user
              :password db-password})

(defn get-datasource [spec]
  (jdbc/get-datasource spec))

(def ds (get-datasource (or db-url 
                            db-spec)))

(def allowed-fields [:first_name :second_name
                     :last_name :gender :dob
                     :oms_number :address])

(defn- project-allowed-fields [patient]
  (select-keys patient allowed-fields))

(defn- prepare-gender [patient]
  (if (contains? patient :gender)
    (update patient :gender as-other)
    patient))

(defn- prepare-patient-data [patient]
  (-> patient
      project-allowed-fields
      prepare-gender))

(defn- init! []
  (jdbc/execute! ds
                 ["create type gender as enum ('male', 'female');
                   create table if not exists patient (id serial primary key,
                                       first_name varchar (20),
                                       second_name varchar (20),
                                       last_name varchar (20),
                                       gender gender,
                                       dob date,
                                       oms_number int,
                                       address varchar (40))"]))

(defn- drop-table! [db]
  (jdbc/execute! db
                 ["drop table patient;
                   drop type gender;"]))

(comment
  (init!)
  (drop-table! ds))

(defn get-patient [id db]
  (sql/get-by-id db :patient id))

(defn get-patients [db]
  (sql/query db ["select * from patient"]))

(defn create-patient! [patient db]
  ((sql/insert! db :patient (prepare-patient-data patient))
   :patient/id))

(defn update-patient! [id patient db]
  (if (seq patient)
    (sql/update! db :patient (prepare-patient-data patient) {:id id})
    {:next.jdbc/update-count 0}))

(defn delete-patient! [id db]
  (sql/delete! db :patient {:id id}))

(defn empty-table! [db]
  (jdbc/execute! db
                 ["TRUNCATE patient"]))

(comment
  (get-patients ds)
  (get-patient 1 ds)
  (create-patient! test ds)
  (update-patient! 10 {:first_name "Richard"} ds)
  (delete-patient! 1 ds)
  (empty-table! ds))
