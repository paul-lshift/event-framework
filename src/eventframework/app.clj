(ns eventframework.app
  (:use 
    [eventframework.commands :only [put-command listen-commands]]
    [compojure.core :only [defroutes context GET PUT]])
  (:require 
    [compojure.handler :as handler]
    [ring.util.response :as response]
    [compojure.route :as route]
    aleph.http
    lamina.core
    cheshire.core))

(defn listen-response [position commands]
  (response/content-type 
    (response/response 
      (cheshire.core/generate-string {
        :position position
        :messages commands
    }))
    "application/json"))

(defn getmsg-handler [channel request] 
  (listen-commands (:position (:params request))
    (fn [position commands] 
      (lamina.core/enqueue channel (listen-response position commands)))))

(defroutes ajax
  (GET "/foo" [] "foo")
  (GET "/getmsg" [] (aleph.http/wrap-aleph-handler getmsg-handler))
  (PUT "/putmsg/:uuid" [uuid message] (do (put-command uuid message) uuid)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
