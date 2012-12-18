(ns eventframework.business-test
   (:use
     eventframework.business
     clojure.test
     midje.sweet))

(deftest business
  (facts
    (subscribed? 
      (->
        initial-state
        (update-state {:type :subscribe :payload {:user "foo" :thread "thread"}}))
      "thread"
      "foo")
    => truthy
    (subscribed? 
      (->
        initial-state
        (update-state {:type :subscribe :payload {:user "foo" :thread "thread"}}))
      "thread"
      "bar")
    => falsey))

