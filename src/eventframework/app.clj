(ns eventframework.app
  (:use 
    [compojure.core :only [defroutes context GET POST]])
  (:require 
    [compojure.handler :as handler]
    [ring.middleware.json :as json-middleware]
    [ring.util.response :as response]
    [compojure.route :as route]
    aleph.http
    lamina.core
    cheshire.core))

(defn modref [f sym]
    (dosync (ref-set sym (f (deref sym)))))

(defn appendref [sym value] (modref #(conj % value) sym))

(defn getset [sym value]
    (dosync (let [old (deref sym)] (do (ref-set sym value) old))))

(def waiting-channels (ref []))

(defn getmsg-handler [channel request] (do
  (prn (deref waiting-channels))
  (prn "Waiting for: " channel)
  (appendref waiting-channels channel)
  (prn (deref waiting-channels))))

(defn broadcast-message [m]
  (doseq [c (getset waiting-channels [])]
    (prn "Sending message to: " c)
    (lamina.core/enqueue c 
      (response/content-type 
        (response/response 
          (cheshire.core/generate-string {
            :messages [m]
            :position "0"
          }))
        "application/json"))))

(defroutes ajax
  (GET "/foo" [] "foo")
  (GET "/getmsg" [] (aleph.http/wrap-aleph-handler getmsg-handler))
  (POST "/putmsg" [message] (do
    (prn (deref waiting-channels))
    (prn "Sending message to channels:" message)
    (broadcast-message message)
    (prn (deref waiting-channels))
    "OK")))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
