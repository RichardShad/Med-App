(ns frontend.components
  [re-com.core :refer [v-box box button h-box border]])

(defn operation-buttons [cur op-lst]
  (if (= cur :read)
    [h-box :size "auto" (mapv (fn [op]
                                [button :label (capitalize (name op))
                                 :on-click #(re-frame/dispatch [::events/set-operation op])])
                              op-lst)]
    [box :size "auto"
     [button :label "Back" :on-click #(re-frame/dispatch [::events/set-operation :read])]]))
