(ns eventframework.scratch
  (:use 
    clojure.test
    midje.sweet
    ring.mock.request
    [compojure.core :only [defroutes context GET PUT]])
  (:require 
    [compojure.handler :as handler]
    [ring.middleware.json :as json-middleware]
    [ring.util.response :as response]
    [compojure.route :as route]))

(deftest start
	(fact "test-test"
	  (+ 2 2) => 4))

(def test-api
  (->
   (handler/api (GET "/foo" [] "foo"))
    (json-middleware/wrap-json-body)
    (json-middleware/wrap-json-response)))

(def test-ui
    (handler/site (GET "/" [] "Hello World")))

(defn filematch [rexp] #(re-find rexp (slurp %)))

(defroutes test-chain
  (context "/api" [] test-api)
  test-ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(deftest ta
	(facts "test-app"
	  (test-chain (request :get "/"))
	    => (contains {
	        :status 200
	        :body #"Hello World"})
	  (test-chain (request :get "/api/foo"))
	    => (contains {
	        :status 200
	        :body #"foo"})
	  (test-chain (request :get "/js/app.js"))
	    => (contains {
	        :status 200
	        :body (filematch #"rubbish-here")})
	  (test-chain (request :get "/does-not-exist"))
	    => (contains {
	        :status 404
	        :body (filematch #"typos")})))

