(ns spaces-central-api.storage.db
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]))

(timbre/refer-timbre)

(defrecord Datomic [name schema]
  component/Lifecycle

  (start [this]
    (info "Starting Datomic")
    (let [uri (str "datomic:mem://" name)]
      (d/delete-database uri)
      (d/create-database uri)
      (let [conn (d/connect uri)
            schema (load-file schema)]
        (d/transact conn schema)
        (assoc this :connection conn))))

  (stop [this]
    (info "Stopping Datomic")
    (dissoc this :connection)))

(defn datomic [name schema]
  (map->Datomic {:name name :schema schema}))
