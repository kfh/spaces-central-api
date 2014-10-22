(ns spaces-central-api.storage.db
  (:require [datomic.api :as d]
            [hara.common :refer [uuid]] 
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defrecord Datomic [env name schema]
  component/Lifecycle

  (start [this]
    (info "Starting Datomic")
    (if (:conn this) 
      this
      (let [uri (str (:datomic-uri env) name)]
        (if (d/create-database uri)
          (let [conn (d/connect uri)
                schema (load-file schema)]
            (d/transact conn schema)
            (assoc this :conn conn))
          (assoc this :conn (d/connect uri))))))

  (stop [this]
    (info "Stopping Datomic")
    (if-not (:conn this) 
      this
      (do  
        (d/release (:conn this))
        (dissoc this :conn)))))

(defrecord DatomicTest [name schema]
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
  (component/using 
    (map->Datomic {:name name :schema schema})
    [:env]))

(defn datomic-test []
  (map->DatomicTest {:name (str uuid) :schema "resources/spaces-central-api-schema.edn"}))
