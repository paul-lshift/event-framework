(ns eventframework.commands-test
  (:use
    eventframework.commands
    clojure.test
    midje.sweet))

(defmacro with-clear-commands [& body]
  `(dosync (ref-set command-state (starting-state))
           ~@body))

(defn append-callback [sym]
  (fn [position commands] (do
    (dosync (ref-set sym (into (deref sym) commands)))
    (apply-or-enqueue-listener! position (append-callback sym)))))

(defn get-new-commands [position]
  (let [[_ commands] (get-next-position-and-commands-from
                      (deref command-state)
                      position)]
    commands))

(deftest channel-test
  (facts "commands"
    (with-clear-commands
      (put-command "uuid" "foo")
      (get-new-commands initial-position))
    => ["foo"]
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (deref res)))
    => []
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (put-command "uuid" "foo")
        (deref res)))
    => ["foo"]
    (with-clear-commands
      (put-command "uuid" "foo")
      (put-command "uuid2" "foo")
      (get-new-commands initial-position))
    => ["foo", "foo"]
    (with-clear-commands
      (put-command "uuid" "foo")
      (put-command "uuid" "foo")
      (get-new-commands initial-position))
    => ["foo"]
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (put-command "uuid" "foo")
        (put-command "uuid2" "foo")
        (deref res)))
    => ["foo", "foo"]
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (put-command "uuid" "foo")
        (put-command "uuid" "foo")
        (deref res)))
    => ["foo"]))
