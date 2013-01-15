(ns eventframework.business-test
   (:use
     eventframework.business
     eventframework.apply-business
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
                :id "1"
                :body {:title "title"}}
               {:type :subscribe
                :id "2"
                :body {:user "user" :thread "1"}}
               {:type :message
                :id "3"
                :body {:thread "1" :message "foo"}}]))
    =>
    [{:type :newthread, :id "1", :body {:title "title"}}
     {:type :subscribe
      :id "2",
      :body {:thread "1", :user "user"},
      :extraevents [],
      }
     {:body {:thread "1", :message "foo"}, :id "3", :type :message}]))
