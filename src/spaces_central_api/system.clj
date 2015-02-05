(ns spaces-central-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-central-api.storage.db :as db]
            [spaces-central-api.web.sente :as sente]
            [spaces-central-api.env.variables :as env]
            [spaces-central-api.web.routes :as routes]
            [spaces-central-api.web.server :as server]
            [com.stuartsierra.component :as component]  
            [spaces-central-api.storage.queue :as queue]    
            [spaces-central-api.web.handler :as handler]
            [spaces-central-api.logger.loggers :as logger]
            [spaces-central-api.gateway.geocoder :as geocoder]
            [spaces-central-api.storage.publisher :as publisher]
            [spaces-central-api.storage.subscriber :as subscriber]))

(timbre/refer-timbre)

(def config (-> "resources/spaces-central-api-conf.edn" 
                (slurp)
                (clojure.edn/read-string)))

(defn spaces-test-geocoder []
  (component/system-map
      :geocoder (geocoder/google)))

(defn spaces-test-db []
  (component/system-map
    :datomic (db/datomic-test)))

(defn spaces-test-system []
  (component/system-map
    :datomic (db/datomic-test)
    :geocoder (geocoder/google)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :channel-sockets (sente/channel-sockets)
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [db-name db-schema web-host web-port]} config]
    (component/system-map
      :logger (logger/rolling-file-appender)
      :env (env/environment)
      :datomic (db/datomic db-name db-schema)
      :publisher (publisher/tx-report-publisher)
      :subscriber (subscriber/tx-report-subscriber)
      :zeromq (queue/zeromq) 
      :geocoder (geocoder/google)  
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :channel-sockets (sente/channel-sockets)
      :web-server (server/web-server web-host web-port))))

(defn -main [& args]
  (component/start
    (spaces-system config))
  (info "Spaces central api up and running"))
