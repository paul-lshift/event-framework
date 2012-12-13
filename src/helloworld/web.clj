(ns helloworld.web
  (:use [compojure.core :only [defroutes GET PUT]])
  (:require [aleph http]
            [lamina core]))

(defn modref [f sym]
    (dosync (ref-set sym (f (deref sym)))))

(defn appendref [sym value] (modref #(conj % value) sym))

(defn getset [sym value]
    (dosync (let [old (deref sym)] (do (ref-set sym value) old))))

; FIXME: atomicity of answering these

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
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "Hello from Heroku"})
   (GET "/waitfor" [] (aleph.http/wrap-aleph-handler waitfor-handler))
   (GET "/freeall" [] (do 
        (prn (deref waiting-channels))
        (prn "Freeing channels")
        (freeall)
        (prn (deref waiting-channels))
        "They are free")))

(defn -main [port]
  (aleph.http/start-http-server 
    (aleph.http/wrap-ring-handler app)
    {:port (Integer. port)}))

