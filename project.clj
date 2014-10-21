(defproject spaces-central-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[ring "1.3.1"]
                 [environ "1.0.0"]
                 [clj-http "1.0.0"]
                 [compojure "1.1.9"]
                 [liberator "0.12.2"]
                 [ring-transit "0.1.2"]
                 [im.chit/ribol "0.3.3"]
                 [geocoder-clj "0.2.5"]
                 [prismatic/schema "0.3.0"]
                 [org.clojure/clojure "1.6.0"]
                 [im.chit/hara.common "2.1.3"] 
                 [com.taoensso/timbre "3.3.1"]
                 [com.stuartsierra/component "0.2.2"] 
                 [com.datomic/datomic-free "0.9.4894"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :plugins [[lein-ring "0.8.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.6"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:main spaces-central-api.system
                       :aot [spaces-central-api.system]}})
