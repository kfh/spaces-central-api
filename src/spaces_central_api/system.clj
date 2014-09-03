(ns spaces-central-api.system
  (:require [com.stuartsierra.component :as component]
            [spaces-central-api.storage.db :as db]
            [spaces-central-api.web.routes :as routes]
            [spaces-central-api.web.handler :as handler]
            [spaces-central-api.web.server :as server]))  

(defn spaces-test-db []
  (component/system-map
      :datomic (db/datomic-test)))

(defn spaces-test-system []
  (component/system-map
    :datomic (db/datomic-test)
    :api-routes (routes/api-routes)
    :ring-handler (handler/ring-handler)
    :web-server (server/web-server-test)))   

(defn spaces-system [config]
  (let [{:keys [db-name db-schema http-host http-port]} config]
    (component/system-map
      :datomic (db/datomic db-name db-schema)
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
         :http-host "0.0.0.0"
         :http-port 4444}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))
