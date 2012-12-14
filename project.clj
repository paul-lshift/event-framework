(defproject eventframework "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.1"]
                 [ring/ring-devel "1.1.0"]
                 [aleph "0.2.2"]
                 [ring/ring-json "0.1.2"]
                 [midje "1.4.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :profiles {
    :dev {:dependencies [[ring-mock "0.1.3"]]
          :plugins [[lein-midje "2.0.2"]]}})
