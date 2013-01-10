(ns eventframework.app
  (:use
    [eventframework.commands :only [valid-position? put-command! destroy-the-world!]]
    [eventframework.business :only [listen-events!]]
    [compojure.core :only [defroutes context GET PUT POST]])
  (:require
    [compojure.handler :as handler]
    [ring.util.response :as response]
    [compojure.route :as route]
    aleph.http
    lamina.core
    cheshire.core))

(defn get-events [user position]
  (if (not (valid-position? position))
    (cheshire.core/generate-string {:goaway true})
    (let [ch (lamina.core/channel)]
      (listen-events! user
                      position
                      (fn [position events]
                        (lamina.core/enqueue ch
                                             (cheshire.core/generate-string
                                              {:position position
                                               :events   events}))
                        (lamina.core/close ch)))
      ch)))

(defroutes ajax
  (GET "/foo" [] "foo")

  (GET "/events/:user/:position" [user position]
       {:status  200
        :headers {"content-type" "application/json"}
        :body    (get-events user position)})

  (PUT "/command/:type/:id"
       {route-params :route-params
        form-params  :form-params
        remote-addr  :remote-addr}
       (let [{type :type id :id} route-params]
         (put-command! id
                      {:type        (keyword type)
                       :id          id
                       :remote-addr remote-addr
                       :body        (zipmap (map keyword
                                                 (keys form-params))
                                            (vals form-params))})
         id))
  
  (POST "/destroy-the-world" [] (destroy-the-world!)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
