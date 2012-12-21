(ns eventframework.business
  (:require eventframework.commands))

;; --------- Business logic

(def initial-state {:thread-subscriptions {} :threads {}})

(defn update-state-or-nil [state command]
  (let [{type :type payload :payload} command]
    (case type
      :subscribe (let [{user :user thread :thread} payload]
                   (update-in state [:thread-subscriptions thread user] (fn [_] true)))
      :newthread state
      :message (let [{thread :thread} payload]
                 (update-in state [:threads thread] (fn [v] ((fnil conj []) v command))))
      nil)))

(defn subscribed? [state thread user] 
  (get-in state [:thread-subscriptions thread user]))

(defn gen-events [eventuser state command]
  (let [{type :type payload :payload} command]
    (case type
      :subscribe (let [{user :user thread :thread} payload]
                   (if (and (= user eventuser) (not (subscribed? state thread eventuser)))
                     [(assoc command :extraevents (get-in state [:threads thread] []))]
                     []))
      :newthread [command]
      :message (let [{thread :thread} payload]
                 (if (subscribed? state thread eventuser)
                   [command]
                   []))
      [])))

;; ----------- Generic stuff not dependent on exact business logic

(defn update-state [state command]
  (or (update-state-or-nil state command) state))

(defn get-state [position]
  (reduce update-state initial-state
          (eventframework.commands/get-commands-to position)))

(defn reduce-command [user [state eventlist] command]
  (let [newstate (update-state-or-nil state command)]
    (if (nil? newstate)
      [state eventlist] ; Ignore invalid commands
      [newstate (into eventlist (gen-events user state command))])))

(defn reduce-commands [user state commands]
  (reduce #(reduce-command user %1 %2) [state []] commands))

(defn do-listen-events [user position state callback]
  (eventframework.commands/listen-commands
   position
   (fn [newpos commands]
     (let [[newstate events] (reduce-commands user state commands)]
       (if (not-empty events)
         (callback newpos events)
         (do-listen-events user newpos newstate callback))))))

(defn listen-events [user position callback]
  (do-listen-events user position (get-state position) callback))
