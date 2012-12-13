(ns helloworld.web
  (:use [compojure.core :only [defroutes GET]])
  (:require [aleph http]))

(defroutes app
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "Hello from Heroku"}))

(defn -main [port]
  (aleph.http/start-http-server 
    (aleph.http/wrap-ring-handler app)
    {:port (Integer. port)}))

