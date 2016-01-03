(ns spaces-central-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-central-api.storage.db :as db]
            [spaces-central-api.env.variables :as env]
            [spaces-central-api.web.server :as server]
            [com.stuartsierra.component :as component]
            [spaces-central-api.web.handler :as handler]
            [spaces-central-api.storage.queue :as queue]    
            [spaces-central-api.logger.loggers :as logger]
            [spaces-central-api.gateway.geocoder :as geocoder]
            [spaces-central-api.storage.publisher :as publisher]
            [spaces-central-api.storage.subscriber :as subscriber]
            [reloaded.repl :refer [system init start stop go reset]]))

(timbre/refer-timbre)

(def config (-> "resources/spaces-central-api-conf.edn" 
                (slurp)
                (clojure.edn/read-string)))

(defn spaces-test-geocoder []
  (component/system-map
      :geocoder (geocoder/google)))

(defn spaces-test-db []
  (component/system-map
    :db (db/datomic-test)))

(defn spaces-test-system []
  (component/system-map
    :db (db/datomic-test)
    :geocoder (geocoder/google)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [db-name db-schema web-host web-port]} config]
    (component/system-map
      :logger (logger/rolling-file-appender)
      :env (env/environment)
      :db (db/datomic db-name db-schema)
      :tx-report-publisher (publisher/tx-report-publisher)
      :topic-publisher (publisher/topic-publisher)
      :subscriber (subscriber/geolocations-subscriber)
      :hornetq-geolocations (queue/hornetq-geolocations) 
      :geocoder (geocoder/google)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server web-host web-port))))

(defn -main []
  (reloaded.repl/set-init! #(spaces-system config))
  (go)
  (info "Spaces central api up and running"))

