(ns eventframework.business
  (:require eventframework.commands)
  (:require [clojure.tools.logging :as log]))

;; --------- Business logic

(defrecord ThreadsAndSubscriptions
    [threads subscriptions])

(defrecord CommandResult
    [new-state events])

(defrecord Message
    [id type body])


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

(defn reduce-non-nil
  "Like reduce, but skipping over values for which f yields nil.

  (nil results are logged as errors)"
  [f & args]
  (apply reduce
         (fn [x y] (if-let [x' (f x y)]
                     x'
                     (do (log/errorf "Fn ~s returned nil on ~s, keeping ~s"
                                     f y x)
                         x)))
         args))

;; ----------- Generic stuff not dependent on exact business logic

(defn state-at [position]
  (reduce-non-nil update-state initial-state
                  (eventframework.commands/get-commands-to position)))

(defn apply-commands [user state commands]
  (let [apply-command
        (fn [{state :new-state events :events} command]
          (when-let [new-state (update-state state command)]
            (CommandResult. new-state (into events (gen-events user state command)))))]
   (reduce-non-nil apply-command
                   (CommandResult. state [])
                   commands)))

(defn listen-events
  ([user position callback]
     (listen-events user position callback (state-at position)))
  ([user position callback state]
     (eventframework.commands/listen-commands
      position
      (fn [new-pos commands]
        (let [{:keys [new-state events]} (apply-commands user state commands)]
          (if (not-empty events)
            (callback new-pos events)
            (listen-events user new-pos callback new-state)))))))
