(ns eventframework.commands-test
  (:use
    eventframework.commands
    eventframework.commands-support
    clojure.test
    midje.sweet))

(defn append-callback [sym]
  (fn [position commands] (do
    (dosync (ref-set sym (into (deref sym) commands)))
    (apply-or-enqueue-listener! position (append-callback sym)))))

(defn get-new-commands [position]
  (let [[_ commands] (get-next-position-and-commands-from
                      (deref *command-state*)
                      position)]
    commands))

(deftest channel-test
  (facts "commands"
    (with-clear-commands
      (put-command! "id" "foo")
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
        (put-command! "id" "foo")
        (deref res)))
    => ["foo"]
    (with-clear-commands
      (put-command! "id" "foo")
      (put-command! "id2" "foo")
      (get-new-commands initial-position))
    => ["foo", "foo"]
    (with-clear-commands
      (put-command! "id" "foo")
      (put-command! "id" "foo")
      (get-new-commands initial-position))
    => ["foo"]
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (put-command! "id" "foo")
        (put-command! "id2" "foo")
        (deref res)))
    => ["foo", "foo"]
    (with-clear-commands
      (let [res (ref [])]
        (apply-or-enqueue-listener! initial-position (append-callback res))
        (put-command! "id" "foo")
        (put-command! "id" "foo")
        (deref res)))
    => ["foo"]))
