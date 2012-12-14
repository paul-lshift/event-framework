(ns eventframework.test.scratch
  (:use 
    clojure.test
    midje.sweet))

(deftest start
	(fact "test-test"
	  (+ 2 2) => 4))

