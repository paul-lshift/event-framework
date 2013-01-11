(defproject eventframework "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-logging-config/clj-logging-config "1.9.10"]
                 [org.clojure/tools.logging "0.2.3"]
                 [compojure "1.1.1"]
                 [ring/ring-devel "1.1.0"]
                 [aleph "0.2.2"]
                 [cheshire "5.0.1"]
                 [ring/ring-json "0.1.2"]
                 [midje "1.4.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :main eventframework.webwrapper
  :profiles {
    :dev {:dependencies [[ring-mock "0.1.3"]]
          :plugins [[lein-midje "2.0.2"]]}})

; Copied in rather than added as dependency:

; https://github.com/broofa/node-uuid/blob/0cdff1fb0bfb063a09254152958c4334d39c34bd/uuid.js
; https://github.com/douglascrockford/JSON-js/blob/43d7836c8ec9b31a02a31ae0c400bdae04d3650d/json2.js
; https://github.com/macek/jquery-serialize-object/blob/300795024639a86b488db60ec0984330b3a7b523/jquery.serialize-object.js
