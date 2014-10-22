(ns spaces-central-api.storage.listener
  (:require [datomic.api :as d]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [ribol.core :refer [manage]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! chan tap untap go-loop]]))

(timbre/refer-timbre)

(defn- index [search-api-url attrs]
  (let [geocodes {:lat (:geocode/latitude attrs) :lon (:geocode/longitude attrs)}
        location {:id (:ad/public-id attrs) :geocodes geocodes}
        post (partial http/post (str search-api-url "/api/locations"))]
    (manage 
      (post {:form-params location :content-type :transit+json :as :transit+json})
      (catch Exception ex
        (warn "External indexing failed: " (.getMessage ex))))))

(defn ->attr-data [txes]
  (->> (d/q 
         '[:find ?geocode ?v 
           :in $ [[?e ?a ?v _ ?added]] 
           :where 
           [?e ?a ?v _ ?added]
           [?a :db/ident ?geocode]] 
         (:db-after txes)
         (:tx-data txes))
       (into {})))

(defn- take-and-index [search-api-url txes]
  (go-loop []
     (index search-api-url (->attr-data (<! txes))) 
    (recur)))

(defrecord TxListener [env watcher]
  component/Lifecycle
  
  (start [this]
    (info "Starting transaction listener")
    (if (:tx-tap this)
      this
      (let [txes (chan)
            tx-tap(tap (:tx-listener watcher) txes)]
        (take-and-index (:search-api-url env) txes)
        (assoc this :tx-tap tx-tap))))
  
  (stop [this]
    (info "Stopping transaction listener")
    (if-not (:tx-tap this)
      this
      (do 
        (untap (:tx-listener watcher) (:tx-tap this))
        (dissoc this :tx-tap)))))

(defn tx-listener []
  (component/using
    (map->TxListener {})
    [:env :watcher]))
