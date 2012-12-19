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
    => falsey
    (reduce-commands
      "user"
      initial-state
      [{:type :newthread
        :uuid "1"
        :payload {:title "title"}}
       {:type :subscribe
        :uuid "2"
        :payload {:user "user" :thread "1"}}
       {:type :message
        :uuid "3"
        :payload {:thread "1" :message "foo"}}])
    => not-nil?))
