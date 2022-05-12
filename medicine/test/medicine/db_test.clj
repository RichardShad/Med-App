(ns medicine.db-test
  (:require [clojure.test :refer :all]
            [medicine.db :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(def db-spec {:dbtype "postgres"
              :dbname "medb_test"
              :user "medadmin"
              :password "123123"})

