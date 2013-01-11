(ns eventframework.webwrapper
  (:gen-class))

(defn -main [& args]
  (require 'eventframework.web)
  (apply (ns-resolve (find-ns 'eventframework.web) '-main) args))
