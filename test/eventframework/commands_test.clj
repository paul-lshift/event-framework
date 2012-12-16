(ns eventframework.commands-test
  (:use 
    eventframework.commands
    clojure.test
    midje.sweet))

(defn append-callback [sym]
  (fn [position commands] (dosync (ref-set sym (into (deref sym) commands)))))
  
(deftest channel-test
  (facts "commands"
    (with-clear-commands
      (put-command "foo")
      ((get-commands initial-position) 1))
    => ["foo"]
    (with-clear-commands
      (let [res (ref [])]
        (listen-commands initial-position (append-callback res))
        (deref res)))
    => []
    (with-clear-commands
      (let [res (ref [])]
        (listen-commands initial-position (append-callback res))
        (put-command "foo")
        (deref res)))
    => ["foo"]))
