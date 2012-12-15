(ns eventframework.test.app
  (:use 
    eventframework.app
    clojure.test
    midje.sweet
    ring.mock.request))

(defn filematch [rexp] #(re-find rexp (slurp %)))

(deftest test-app
  (facts "test-app"
    (app (request :get "/"))
      => (contains {
          :status 200
          :body (filematch #"Index page")})
    (app (request :get "/ajax/foo"))
      => (contains {
          :status 200
          :body #"foo"})
    (app (request :get "/js/app.js"))
      => (contains {
          :status 200
          :body (filematch #"rubbish-here")})
    (app (request :get "/does-not-exist"))
      => (contains {
          :status 404
          :body (filematch #"typos")})))
