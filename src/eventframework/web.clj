(ns eventframework.web
  (:use [compojure.core :only [defroutes GET PUT]])
  (:require [aleph http]
            [lamina core]
            [ring.util.response :as response]
            [compojure.route :as route]))

(defn modref [f sym]
    (dosync (ref-set sym (f (deref sym)))))

(defn appendref [sym value] (modref #(conj % value) sym))

(defn getset [sym value]
    (dosync (let [old (deref sym)] (do (ref-set sym value) old))))

(def waiting-channels (ref []))

(defn waitfor-handler [channel request] (do
    (prn (deref waiting-channels))
    (prn "Waiting for: " channel)
    (appendref waiting-channels channel)
    (prn (deref waiting-channels))))

(defn freeall []
    (doseq [c (getset waiting-channels [])]
        (prn "Freeing: " c)
        (lamina.core/enqueue c {:status 200 :body "I am free"})))

(defroutes app
  (GET "/" []  (response/resource-response "index.html" {:root "public"}))
  (GET "/waitfor" [] (aleph.http/wrap-aleph-handler waitfor-handler))
  (GET "/freeall" [] (do 
    (prn (deref waiting-channels))
    (prn "Freeing channels")
    (freeall)
    (prn (deref waiting-channels))
    "They are free"))
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(defn -main [port]
  (aleph.http/start-http-server 
    (aleph.http/wrap-ring-handler app)
    {:port (Integer. port)}))

