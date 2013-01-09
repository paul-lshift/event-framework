(ns eventframework.business
  (:require eventframework.commands
            [clojure.tools.logging :as log]))

;; --------- Business logic

(defrecord ServerState
    [])

(defrecord CommandResult
    [new-state events])

(def initial-state (ServerState.))

(defn update-state [state command]
  "Produce the next `state` resulting from `command` or nil if invalid. "
  state)

(defn gen-events [eventuser state command] [command])

;; ----------- Generic stuff not dependent on exact business logic

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

(defn state-before [position]
  (reduce-non-nil update-state initial-state
                  (eventframework.commands/get-commands-before position)))

(defn apply-commands [user state commands]
  (let [apply-command
        (fn [{state :new-state events :events} command]
          (when-let [new-state (update-state state command)]
            (CommandResult. new-state (into events (gen-events user state command)))))]
   (reduce-non-nil apply-command
                   (CommandResult. state [])
                   commands)))

(defn listen-events!
  ([user position callback]
     (listen-events! user position callback (state-before position)))
  ([user position callback state]
     (eventframework.commands/apply-or-enqueue-listener!
      position
      (fn [new-pos commands]
        (let [{:keys [new-state events]} (apply-commands user state commands)]
          (if (not-empty events)
            (callback new-pos events)
            (listen-events! user new-pos callback new-state)))))))
