(ns eventframework.app
  (:use 
    [compojure.core :only [defroutes context GET PUT]])
  (:require 
    [compojure.handler :as handler]
    [ring.middleware.json :as json-middleware]
    [ring.util.response :as response]
    [compojure.route :as route]))

(def api
  (->
   (handler/api (GET "/foo" [] "foo"))
    (json-middleware/wrap-json-body)
    (json-middleware/wrap-json-response)))

(def ui
  (handler/site 
    (GET "/" []  (response/resource-response "index.html" {:root "public"}))))
  
(defroutes app
  (context "/api" [] api)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

