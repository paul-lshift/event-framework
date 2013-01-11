(ns eventframework.app
  (:use
    [eventframework.commands :only [valid-position? put-command!]]
    [eventframework.business :only [listen-events!]]
    [compojure.core :only [defroutes context GET PUT]])
  (:require
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
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

  (GET "/fail" [] (throw (RuntimeException. "Fail")))

  (GET "/events/:user/:position" [user position]
       {:status  200
        :headers {"content-type" "application/json"}
        :body    (get-events user position)})

  (PUT "/command/:type/:id"
       {route-params :route-params
        bodyStream   :body
        remote-addr  :remote-addr}
       (let [{type :type id :id} route-params
             body (with-open [rdr (io/reader bodyStream)]
                    (cheshire.core/parse-stream rdr keyword))]
         (put-command! id
                       {:type        (keyword type)
                        :id          id
                        :remote-addr remote-addr
                        :body        body})
         id)))

(defroutes ui
  (GET "/" []  (response/resource-response "index.html" {:root "public"})))

(defroutes towrap
  (context "/ajax" [] ajax)
  ui
  (route/resources "/")
  (route/not-found (response/resource-response "404.html" {:root "error"})))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           (log/error e "processing HTTP request" req)
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "error/500.html"))}))))

(def app (wrap-error-page (handler/site towrap)))
