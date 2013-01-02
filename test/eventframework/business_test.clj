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
      (update-state {:type :subscribe :body {:user "foo" :thread "thread"}}))
     "thread"
     "foo")
    => true
    (subscribed?
     (->
      initial-state
      (update-state {:type :subscribe :body {:user "foo" :thread "thread"}}))
     "thread"
     "bar")
    => false
    (:events (apply-commands
              "user"
              initial-state
              [{:type :newthread
                :uuid "1"
                :body {:title "title"}}
               {:type :subscribe
                :uuid "2"
                :body {:user "user" :thread "1"}}
               {:type :message
                :uuid "3"
                :body {:thread "1" :message "foo"}}]))
    =>
    [{:type :newthread, :uuid "1", :body {:title "title"}}
     {:type :subscribe
      :uuid "2",
      :body {:thread "1", :user "user"},
      :extraevents [],
      }
     {:body {:thread "1", :message "foo"}, :uuid "3", :type :message}]))
