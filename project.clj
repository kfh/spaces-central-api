(defproject spaces-central-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[ring "1.3.2"]
                 [environ "1.0.0"]
                 [clj-http "1.0.1"]
                 [http-kit "2.1.19"]
                 [compojure "1.3.1"]
                 [liberator "0.12.2"]
                 [geocoder-clj "0.2.5"]
                 [ring-transit "0.1.3"]
                 [im.chit/ribol "0.4.0"]
                 [prismatic/schema "0.3.3"]
                 [im.chit/hara.data "2.1.7"] 
                 [com.taoensso/sente "1.2.0"]
                 [org.clojure/clojure "1.6.0"]
                 [im.chit/hara.common "2.1.7"] 
                 [com.taoensso/timbre "3.3.1"]
                 [com.stuartsierra/component "0.2.2"] 
                 [io.clojure/liberator-transit "0.3.0"]
                 [org.immutant/messaging "2.1.0"
                  :exclusions [org.hornetq/hornetq-server
                               org.hornetq/hornetq-journal
                               org.hornetq/hornetq-commons
                               org.hornetq/hornetq-jms-server]]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"] 
                 [com.datomic/datomic-free "0.9.5078"
                  :exclusions [org.slf4j/slf4j-nop
                               joda-time
                               commons-codec
                               org.jboss.logging/jboss-logging]]
                 [org.hornetq/hornetq-jms-server "2.3.17.Final"
                  :exclusions [org.jboss.spec.javax.transaction/jboss-transaction-api_1.1_spec
                               org.jboss.logging/jboss-logging
                               org.jboss/jboss-transaction-spi
                               org.jgroups/jgroups
                               org.jboss.jbossts.jts/jbossjts-jacorb]]]
  :plugins [[lein-ring "0.8.11"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.8"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:main spaces-central-api.system
                       :aot [spaces-central-api.system]}})
