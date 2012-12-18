(ns eventframework.app
  (:use 
    [eventframework.commands :only [is-valid-position put-command listen-commands]]
    [compojure.core :only [defroutes context GET PUT]])
  (:require 
    [compojure.handler :as handler]
    [ring.util.response :as response]
    [compojure.route :as route]
    aleph.http
    lamina.core
    cheshire.core))

(defn json-response [data]
  (response/content-type 
    (response/response (cheshire.core/generate-string data))
    "application/json"))

(defn getevents-handler [channel request] 
  (let [position (:position (:params request))]
    (if (not (is-valid-position position))
      (lamina.core/enqueue channel (json-response {:goaway true}))
      (listen-commands
        position
        (fn [position commands]
          (lamina.core/enqueue channel (json-response {
            :position position
            :events commands
          })))))))

(defroutes ajax
  (GET "/foo" [] "foo")
  (GET "/events" [] (aleph.http/wrap-aleph-handler getevents-handler))
  (PUT "/command/:type/:uuid" [type uuid message] (do 
    (put-command uuid  {:type type :uuid uuid :payload {:message message}})
    uuid)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
