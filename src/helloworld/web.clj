(ns helloworld.web
  (:use compojure.core
        lamina.core
        aleph.http)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "Hello from Heroku"}))


(defn -main [port]
  (start-http-server 
    (wrap-ring-handler app)
    {:port (Integer. port)}))

