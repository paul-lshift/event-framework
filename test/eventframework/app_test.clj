(ns eventframework.app-test
  (:use 
    (eventframework app [commands :only [new-uuid]])
    clojure.test
    midje.sweet
    ring.mock.request)
  (:require
   [cheshire.core :as json]))

(defn filematch [rexp] #(re-find rexp (slurp %)))

(defn json-body [response]
  (json/parse-string (first (lamina.core/channel-seq (:body response))) true))

(defn new-thread-request [command-id text]
  (body (request :put (str "/ajax/command/newthread/" command-id))
        {:text text}))

(defn get-events-request [user position] 
  (request :get (str "/ajax/events/" user "/" position)))

(defn subscribe-request [user thread]
  (body (request :put (str "/ajax/command/subscribe/" (new-uuid)))
        {:user user
         :thread thread}))

(deftest test-app
  (facts "serves static files"
    (app (request :get "/"))
      => (contains {:status 200
                    :body (filematch #"Proof-of-concept")})
    (app (request :get "/ajax/foo"))
      => (contains {:status 200
                    :body #"foo"})
    (app (request :get "/js/app.js"))
      => (contains {:status 200
                    :body (filematch #"readEvents")})
    (app (request :get "/does-not-exist"))
      => (contains {:status 404
                    :body (filematch #"typos")}))
  
  
  (let [thread-command-id      (new-uuid)
        new-thread-response    (app (new-thread-request thread-command-id "Hello World!"))

        {alice-position-1 :position alice-events-1 :events}
        (json-body (app (get-events-request "alice" "0")))
        
        alice-subscribe-response (app (subscribe-request "alice" thread-command-id))

        {alice-position-2 :position alice-events-2 :events}
        (json-body (app (get-events-request "alice" alice-position-1)))]
        
       (fact "allows creation of conversation threads"
         new-thread-response => (contains {:status 200}))

       (fact "distributes new threads to users"
         (count alice-events-1) => 1
         (first alice-events-1) => (contains {:type "newthread"
                                              :id   thread-command-id
                                              :body {:text "Hello World!"}}))))