(defproject spaces-central-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[ring "1.3.0"]
                 [org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [com.stuartsierra/component "0.2.1"]
                 [com.taoensso/timbre "3.2.1"]
                 [com.datomic/datomic-free "0.9.4880.2"]]
  :plugins [[lein-ring "0.8.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
