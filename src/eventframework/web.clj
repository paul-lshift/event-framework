(ns eventframework.web
  (:require eventframework.app
            [aleph http]
            clojure.tools.logging
            [clj-logging-config.log4j :as log-config]
            )
  (:gen-class))


(defn -main [port]
  (log-config/set-logger! :out *err*)
  (aleph.http/start-http-server
   (aleph.http/wrap-ring-handler eventframework.app/app)
   {:port (Integer. port)}))
