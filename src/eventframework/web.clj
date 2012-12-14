(ns eventframework.web
  (:require eventframework.app
            [aleph http]))

(defn -main [port]
  (aleph.http/start-http-server 
    (aleph.http/wrap-ring-handler eventframework.app/app)
    {:port (Integer. port)}))

