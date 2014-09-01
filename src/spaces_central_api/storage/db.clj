(ns spaces-central-api.storage.db
  (:require [datomic.api :as d]
            [hara.common :refer [uuid]] 
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defrecord Datomic [name schema]
  component/Lifecycle

  (start [this]
    (info "Starting Datomic")
    (if (:conn this) 
      this
      (let [uri (str "datomic:mem://" name)]
        (d/delete-database uri)
        (d/create-database uri)
        (let [conn (d/connect uri)
              schema (load-file schema)]
          (d/transact conn schema)
          (assoc this :conn conn)))))

  (stop [this]
    (info "Stopping Datomic")
    (if-not (:conn this) 
      this
      (dissoc this :conn))))

(defn datomic [name schema]
  (map->Datomic {:name name :schema schema}))

(defn datomic-test []
  (map->Datomic {:name (str uuid) :schema "resources/spaces-central-api-schema.edn"}))
