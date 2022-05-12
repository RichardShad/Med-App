(ns medicine.core-test
  (:require [clojure.test :refer :all]
            [medicine.core :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def prop-router-accepts-ints
  (prop/for-all [] true))

(tc/quick-check 100 prop-router-accepts-ints)
