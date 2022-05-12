(ns medicine.handler-test
  (:require [clojure.test :refer :all]
            [medicine.core :refer :all]
            [medicine.routing :refer [make-router]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))