(ns medui.patient.subs
  (:require [re-frame.core :as re-frame]
            [medui.subs :as core-subs]))

(re-frame/reg-sub
 ::operation
 :<- [::core-subs/active-page]
 (fn [page _]
   (:operation page)))

(re-frame/reg-sub
 ::loading-success
 :<- [::core-subs/active-page]
 (fn [page _]
   (:loaded? page)))

(re-frame/reg-sub
 ::patient
 :<- [::core-subs/active-page]
 (fn [page _]
   (:patient page)))

(re-frame/reg-sub
 ::loading
 :<- [::core-subs/active-page]
 (fn [page _]
   (:loading page)))

(re-frame/reg-sub
 ::op-failure
 :<- [::core-subs/active-page]
 (fn [page _]
   (:op-failed? page)))