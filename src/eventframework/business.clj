(ns eventframework.business)

(def initial-state {:thread-subscriptions {}})

(defn add-subscription [state thread user]
  (update-in state [:thread-subscriptions thread user] (fn [x] true)))

(defn update-state-or-nil [state event]
  (case (:type event)
    :subscribe (add-subscription state (:thread (:payload event)) (:user (:payload event)))
    :newthread state
    :message state
    nil))
  
(defn update-state [state event]
  (or (update-state-or-nil state event) state))
  
(defn subscribed? [state thread user] 
  (get-in state [:thread-subscriptions thread user]))
