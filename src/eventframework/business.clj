(ns eventframework.business
  (:require [clojure.tools.logging :as log]))

(defrecord ThreadsAndSubscriptions
    [threads subscriptions])

(def initial-state (ThreadsAndSubscriptions. {} {}))

(defn update-state [state command]
  "Produce the next `state` resulting from `command` or nil if invalid. "
  (let [{type :type body :body} command]
    (case type
      :subscribe (let [{user :user thread :thread} body]
                   (update-in state
                              [:subscriptions thread user]
                              (constantly true)))
      :newthread state
      :message (let [{thread :thread} body]
                 (update-in state
                            [:threads thread]
                            (fn [v] ((fnil conj []) v command))))
      (do (log/warn "Ignoring unknown command type" type "while updating state")
          nil))))

(defn subscribed? [state thread user]
  (boolean (get-in state [:subscriptions thread user])))

(defn gen-events [eventuser state command]
  (let [{type :type body :body} command]
    (case type
      :subscribe (let [{user :user thread :thread} body]
                   (if (and (= user eventuser)
                            (not (subscribed? state thread eventuser)))
                     ;; FIXME(alexander): flatten this
                     [(assoc command :extraevents (get-in state [:threads thread] []))]
                     []))
      :newthread [command]
      :message (let [{thread :thread} body]
                 (if (subscribed? state thread eventuser)
                   [command]
                   []))
      (do (log/warn "Ignoring unknown command type" type "while gen. events")
          []))))
