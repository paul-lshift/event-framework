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
    => true
    (subscribed?
     (->
      initial-state
      (update-state {:type :subscribe :payload {:user "foo" :thread "thread"}}))
     "thread"
     "bar")
    => false
    (:events (apply-commands
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
                :payload {:thread "1" :message "foo"}}]))
    =>
    [{:type :newthread, :uuid "1", :payload {:title "title"}}
     {:type :subscribe
      :uuid "2",
      :payload {:thread "1", :user "user"},
      :extraevents [],
      }
     {:payload {:thread "1", :message "foo"}, :uuid "3", :type :message}]))
