(ns medui.helpers
  (:require [clojure.string :as str])
  (:require-macros [adzerk.env :as env]))

(env/def API_URL "https://rocky-beach-55793.herokuapp.com")

(defn endpoint [& path]
  (str/join "/" (cons (str API_URL "/api/v1") path)))

(defn filter-kv [pred map]
  (reduce-kv (fn [accumulator key value]
               (if (pred key value)
                 (assoc accumulator key value)
                 accumulator)) {} map))

(defn prepare-gender [p]
  (if (contains? p :gender)
    (update p :gender #{"male" "female"}) 
    p))

(defn prepare-dob [p]
  (if (:dob p)
    (update p :dob #(. (js/Date.
                        (. js/Date parse %))
                       toISOString))
    p))

(defn prepare-oms-num  [p]
  (if (:oms-number p)
    (update p :oms-number int)
    p))

(defn prepare-patient-data [p]
  (-> p
      prepare-gender
      (->> (filter-kv (fn [_ v] (seq v))))
      prepare-dob
      prepare-oms-num))
