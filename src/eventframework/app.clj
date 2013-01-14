(ns eventframework.app
  (:use
    [eventframework.commands :only [valid-position? put-command! get-all-commands set-commands!]]
    [eventframework.business :only [listen-events!]]
    [compojure.core :only [defroutes context GET PUT POST]])
  (:require
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [compojure.handler :as handler]
    [ring.util.response :as response]
    [compojure.route :as route]
    clj-time.core
    clj-time.format
    aleph.http
    lamina.core
    cheshire.core))

(defn- iso-now []
  (clj-time.format/unparse
    (clj-time.format/formatters :date-time-no-ms)
    (clj-time.core/now)))

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

(defn- read-json [body]
  (with-open [rdr (io/reader body)]
    (cheshire.core/parse-stream rdr true)))

(defn- fix-type [command]
  (update-in command [:type] keyword))

(defroutes ajax
  (GET "/foo" [] "foo")

  (GET "/fail" [] (throw (RuntimeException. "Fail")))

  (GET "/commands" []
       {:status 200
        :headers {"content-type" "application/json"}
        :body (cheshire.core/generate-string (get-all-commands) {:pretty true})})

  (PUT "/commands"
       {body :body}
       (do
         (set-commands! (vec (map fix-type (read-json body))))
         "Success"))
  
  (POST "/commands-append"
       {body :body}
       (do
	       (doseq [command (vec (map fix-type (read-json body)))]
	         (put-command! command))
         "Success"))
        
       
  (GET "/events/:user/:position" [user position]
       {:status  200
        :headers {"content-type" "application/json"}
        :body    (get-events user position)})

  (PUT "/command/:type/:id"
       {route-params :route-params
        body         :body
        remote-addr  :remote-addr}
       (let [{type :type id :id} route-params]
         (put-command! {:type        (keyword type)
                        :id          id
                        :date        (iso-now)
                        :remote-addr remote-addr
                        :body        (read-json body)})
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
