(ns backend.conf
  (:require [clojure.string :as str]))

(defn get-env [name]
  (System/getenv name))

(defn get-env-int [name]
  (when-let [val (get-env name)]
    (Integer/parseInt val)))

(def port (or (get-env-int "PORT")
              3000))

(def db-name (or (get-env "DB_NAME")
                 "medb"))

(def db-user (or (get-env "DB_USER")
                 "medadmin"))

(def db-password (or (get-env "DB_PASSWORD")
                     "123123"))

(def client-url (or
                 (get-env "CLIENT_URL")
                 "https://hoppscotch.io"))

(def db-url (let [url (get-env "DATABASE_URL")]
              (when url (str/replace url #"^postgres:\/\/(\w+):(\w+)@([-\w.]+):(\d+)\/(\w+)"
                                     "jdbc:postgresql://$3:$4/$5?user=$1&password=$2"))))
