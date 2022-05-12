(ns medui.components
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [medui.events :as events]
            [medui.subs :as subs]
            [goog.string :as gstring]
            [goog.string.format]))

(defn- Y [f] (fn [& args] (apply f f args)))

(def patient-fields [:first-name :last-name :second-name
                     :oms-number :dob :address :gender])

(defn get-input-value [id]
  (.. js/document (getElementById (name id)) -value))

(defn get-input-values [ids]
  (let [values (mapcat (fn [k] [k (get-input-value k)]) ids)]
    (apply hash-map values)))

(defn get-patient-name [{:keys [last-name second-name first-name]}]
  (str/join " " [last-name first-name second-name]))

(defn get-patient-oms-num [{oms :oms-number}]
  (let [nums (map (partial apply str) (partition 4 (str oms)))]
    (str/join " "  nums)))

(defn with-tag [tag view arg]
  [tag [view arg]])

(defn view-pass-argument [tag arg & view-lst]
  (loop [acc [tag] [cur & rst] view-lst]
    (let [res (if cur
                (conj acc
                      (conj cur arg))
                acc)]
      (if rst
        (recur res rst)
        res))))

(defn button [label on-click]
  [:button {:on-click on-click} label])

(defn make-input-submit [eff field-lst]
  [button "Submit" #(re-frame/dispatch (into eff [(get-input-values field-lst)]))])

(defn patient-sumbit [eff-name] 
  (make-input-submit eff-name patient-fields))

(defn if-view [b t f]
  (if b
    t
    f))

(defn select-gender
  [default]
  [:select 
   {:name "gender"
    :id "gender"
    :default-value default}
   [:option {:value nil} "None"]
   [:option {:value "male"} "Male"]
   [:option {:value "female"} "Female"]])

(defn make-input-field-of [type k]
  (Y (fn ([f m]
          (assoc-in (f f) [1 :default-value] (k m)))
       ([_]
        (let [n (name k)]
          [:input {:type type
                   :id n
                   :name n
                   :placeholder (str/replace n #"-" " ")}])))))

(defn make-text-input-field [k]
  (make-input-field-of "text" k))

(defn date-str->input-date [dstr]
  (let [date (js/Date. (. js/Date parse dstr))]
    (gstring/format "%04d-%02d-%02d"
                    (.getFullYear date)
                    (inc (.getMonth date))
                    (.getDate date))))

(defn patient-dob->input-date [patient]
  (update patient :dob date-str->input-date))

(defn make-date-input-field [k]
  (comp (make-input-field-of "date" k)
        patient-dob->input-date))

(defn operation-view [first after-fail]
  (let [op-success @(re-frame/subscribe [::subs/operation-success])]
    (if op-success
      first
      after-fail)))

(defn loaded-view [if-load-success]
  (let [updated @(re-frame/subscribe [::subs/loaded])]
    (if-view updated if-load-success "Loading failed.")))

(defn loading-view [if-loaded]
  (let [loading @(re-frame/subscribe [::subs/loading])]
     (if loading "Loading..." if-loaded)))

(defn date-view [date]
  (let [options {:year "numeric" :month "numeric" :day "numeric"}
        date-str (. (js/Date. (. js/Date parse date)) toLocaleDateString "ru-RU" options)]
    date-str))

(defn patient-name-view
  ([patient]
   [:p (get-patient-name patient)])
  ([before patient]
   [:p before (get-patient-name patient)]))

(def patient-first-name-input (make-text-input-field :first-name))

(def patient-second-name-input (make-text-input-field :second-name))

(def patient-last-name-input (make-text-input-field :last-name))

(defn patient-name-input 
  ([]
   [:div
    [patient-last-name-input]
    [patient-first-name-input]
    [patient-second-name-input]])
  
  ([patient]
   (view-pass-argument :div patient
                       [patient-last-name-input]
                       [patient-first-name-input]
                       [patient-second-name-input])))

(defn patient-gender-view
  ([patient]
   [:p (:gender patient)])
  ([before patient]
   [:p before (:gender patient)]))

(defn patient-gender-select
  ([]
   (select-gender nil))
  ([patient]
   (select-gender (:gender patient))))

(defn patient-oms-num-view
  ([patient]
   [:p (get-patient-oms-num patient)])
  ([before patient]
   [:p before (get-patient-oms-num patient)]))

(def patient-oms-num-input (make-text-input-field :oms-number))

(defn patient-dob-view
  ([{:keys [dob]}]
   [:p [date-view dob]])
  ([before {:keys [dob]}]
   [:p before [date-view dob]]))

(def patient-dob-input (make-date-input-field :dob))

(defn patient-address-view
  ([{:keys [address]}]
   [:p address])
  ([before {:keys [address]}]
   [:p before address]))

(def patient-address-input (make-text-input-field :address))

(defn patient-input [patient]
  (view-pass-argument :div patient
                      [patient-name-input]
                      [with-tag :div patient-address-input] 
                      [with-tag :div patient-oms-num-input]
                      [with-tag :div patient-dob-input]
                      [with-tag :div patient-gender-select]))

(defn make-choice-view [m]
  (fn [op & args]
    (into (vec (or (op m) (:default m))) args)))

(defn op-button [name operation]
  [button name #(re-frame/dispatch [::events/set-operation operation])])

(defn refresh-button [update-event]
  [button "Refresh" #(re-frame/dispatch update-event)])

(defn make-option-panel [option-lst choice-view]
  (fn [& args]
    (let [op @(re-frame/subscribe [::subs/operation])
          render-button (fn [op] [op-button
                                  (str/capitalize (name op))
                                  op])]
      [:div (into [choice-view op] args)
       (if (some #{op} option-lst)
         [:div [op-button "Back" :default]]
         (into [:div] (mapv render-button option-lst)))])))
