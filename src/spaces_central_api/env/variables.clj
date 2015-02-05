(ns spaces-central-api.env.variables
    (:require [environ.core :refer [env]]
              [ribol.core :refer [raise]]
              [taoensso.timbre :as timbre]
              [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defn- assemble-datomic-uri [variables] 
  (if-let [datomic-uri (:datomic-uri env)]
    (assoc variables :datomic-uri datomic-uri)
    (raise {:error "Env variable DATOMIC_URI not set!"})))

(defrecord Environment []
  component/Lifecycle 
  
  (start [this]
    (info "Assembling Environment")
    (cond->
      this
      ((complement :datomic-uri) this) (assemble-datomic-uri)))
  
  (stop [this]
    (info "Disassembling Environment")
    (cond-> 
      this
      (:datomic-uri this) (dissoc :datomic-uri))))

(defn environment []
  (component/using 
    (map->Environment {})
     [:logger]))

(defn environment-without-logger []
  (map->Environment {}))
