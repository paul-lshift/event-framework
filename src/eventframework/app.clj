(ns eventframework.app
  (:use
    [eventframework.commands :only [is-valid-position put-command]]
    [eventframework.business :only [listen-events]]
    [compojure.core :only [defroutes context GET PUT]])
  (:require
    [compojure.handler :as handler]
    [ring.util.response :as response]
    [compojure.route :as route]
    aleph.http
    lamina.core
    cheshire.core))

(defn getevents [user position]
  (if (not (is-valid-position position))
    (cheshire.core/generate-string {:goaway true})
    (let [ch (lamina.core/channel)]
      (listen-events user
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
        :body    (getevents user position)})

  (PUT "/command/:type/:uuid"
       {route-params :route-params
        form-params  :form-params
        remote-addr  :remote-addr}
       (let [{type :type uuid :uuid} route-params]
         (put-command uuid
                      {:type        (keyword type)
                       :uuid        uuid
                       :remote-addr remote-addr
                       :body        (zipmap (map keyword
                                                 (keys form-params))
                                            (vals form-params))})
         uuid)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(def app (handler/site towrap))
