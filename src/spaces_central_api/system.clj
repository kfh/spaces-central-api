(ns spaces-central-api.system
  (:gen-class)
  (:require [taoensso.timbre :as timbre] 
            [spaces-central-api.storage.db :as db]
            [spaces-central-api.web.routes :as routes]
            [spaces-central-api.web.handler :as handler]
            [spaces-central-api.web.server :as server]
            [spaces-central-api.gateway.geocoder :as geocoder]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

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
    :web-server (server/web-server-test)))

(defn spaces-system [config]
  (let [{:keys [db-name db-schema http-host http-port]} config]
    (component/system-map
      :datomic (db/datomic db-name db-schema)
      :geocoder (geocoder/google)  
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server http-host http-port))))

(def system nil)

(defn init []
  (alter-var-root
    #'system
    (constantly
      (spaces-system
        {:db-name "spaces"
         :db-schema "resources/spaces-central-api-schema.edn"
         :http-host "127.0.0.1"
         :http-port 4444}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn -main [& args]
  (init)
  (start)
  (info "Spaces central api up and running"))
