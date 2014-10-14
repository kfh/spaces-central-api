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

(defn- assemble-search-api-url [variables] 
  (if-let [search-api-url (:spaces-search-api-url env)]
    (assoc variables :search-api-url search-api-url)
    (raise {:error "Env variable SPACES_SEARCH_API_URL not set!"})))

(defrecord Environment []
  component/Lifecycle 
  
  (start [this]
    (info "Assembling Environment")
    (cond->
      this
      ((complement :datomic-url) this) (assemble-datomic-uri)
      ((complement :search-api-url) this) (assemble-search-api-url)))
  
  (stop [this]
    (info "Disassembling Environment")
    (cond-> 
      this
      (:datomic-uri this) (dissoc :datomic-uri)
      (:search-api-url this) (dissoc :search-api-url))))

(defrecord EnvironmentTest []
  component/Lifecycle 
  
  (start [this]
    (info "Assembling Environment")
    (if (:search-api-url this)
      this
      (assemble-search-api-url this)))
  
  (stop [this]
    (info "Disassembling Environment")
    (if-not (:search-api-url this)
      this
      (dissoc this :search-api-url))))

(defn environment []
  (map->Environment {}))

(defn environment-test []
  (map->EnvironmentTest {}))
