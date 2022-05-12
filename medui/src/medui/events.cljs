(ns medui.events
  (:require
   [re-frame.core :as re-frame]
   [medui.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::set-values
 (fn [db [_ values]]
   (assoc db :values values)))

(re-frame/reg-event-db
 ::set-operation
 (fn [db [_ op]]
   (assoc-in db [:active-page :operation] op)))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx
  ::navigate
  (fn-traced [_ [_ handler]]
   {:navigate handler}))

(re-frame/reg-event-db
 ::set-active-page
 (fn [db [_ active-page]]
   (assoc db :active-page active-page)))