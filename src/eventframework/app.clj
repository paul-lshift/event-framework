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
  (PUT "/command/:type/:uuid" {
      route-params :route-params
      form-params :form-params
      remote-addr :remote-addr
    }
    (let [{type :type uuid :uuid} route-params]
      (put-command uuid {
        :type (keyword type)
        :uuid uuid
        :remote-addr remote-addr
        :payload (zipmap (map keyword (keys form-params)) (vals form-params))
      })
      uuid)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
