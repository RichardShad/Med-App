(ns medui.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-page
 (fn [db _]
   (db :active-page)))

(re-frame/reg-sub
 ::active-panel
 :<- [::active-page]
 (fn [page _]
   (:panel page)))

(re-frame/reg-sub
 ::loading
 :<- [::active-page]
(fn [page _]
  (:loading page)))

(re-frame/reg-sub
 ::loaded
 :<- [::active-page]
(fn [page _]
  (:loaded page)))

(re-frame/reg-sub
 ::operation
 :<- [::active-page]
 (fn [page _]
   (:operation page)))

(re-frame/reg-sub
 ::operation-success
 :<- [::active-page]
 (fn [page _]
   (:op-success page)))