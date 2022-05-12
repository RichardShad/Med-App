(ns medui.patients.subs
  (:require [re-frame.core :as re-frame]
            [medui.subs :as core-subs]))

(re-frame/reg-sub
 ::patients
 :<- [::core-subs/active-page]
 (fn [page _]
   (:patients page)))