(ns spaces-central-api.system
  (:require [com.stuartsierra.component :as component]
            [spaces-central-api.storage.db :as db]
            [spaces-central-api.web.routes :as routes]
            [spaces-central-api.web.handler :as handler]
            [spaces-central-api.web.server :as server]))  

(defn spaces-system [config]
  (let [{:keys [db-name db-schema http-port]} config]
    (component/system-map
      :datomic (db/datomic db-name db-schema)
      :api-routes (routes/api-routes)
      :ring-handler (handler/ring-handler)
      :web-server (server/web-server http-port))))

(def system nil)

(defn init []
  (alter-var-root 
    #'system
    (constantly 
      (spaces-system 
        {:db-name "spaces" 
         :db-schema "resources/spaces-central-api-schema.edn" 
         :http-port 4444}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))
