(ns eventframework.commands-support
  (:use
    [eventframework.commands :only [*command-state* starting-state]]))

(defmacro with-clear-commands [& body]
  `(binding [*command-state* (ref (starting-state))]
    (do ~@body)))

