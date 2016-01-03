(ns spaces-central-api.storage.publisher
  (:require [datomic.api :as d]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [>!! <!! chan close! pub thread]]))

(timbre/refer-timbre)

(defn- take-geolocations [tx-report] 
  (keep 
    (fn [[e a v t added]]
      (let [db (if added (:db-after tx-report) (:db-before tx-report))
            entity (partial d/entity db)] 
        (when (= :location/geocode (-> a entity :db/ident))
          (let [{:keys [:geocode/latitude :geocode/longitude]} (entity v)
                public-id '[{:real-estate/_location [{:ad/_real-estate [:ad/public-id]}]}]]
            {:id (->> e
                      (d/pull db public-id) 
                      :real-estate/_location 
                      :ad/_real-estate 
                      :ad/public-id) 
             :geocodes {:lat latitude :lon longitude}
             :timestamp (->> t 
                             (d/entity (:db-after tx-report)) 
                             :db/txInstant) 
             :added added})))) 
    (:tx-data tx-report)))

(defn- publish-geolocations [tx-report topic-publisher]
  (let [geolocations (take-geolocations tx-report)]
    (when-not (empty? geolocations)
      (>!! topic-publisher {:topic :geolocations :data geolocations}))))

(defrecord TxReportPublisher [db]
  component/Lifecycle

  (start [this]
    (info "Starting transaction report publisher")
    (if (:tx-publisher this) 
      this
      (let [tx-publisher (chan)
            tx-queue (d/tx-report-queue (:conn db))]
        (thread 
          (while true
            (when-let [tx-report (.take tx-queue)]
              (>!! tx-publisher tx-report))))
        (assoc this :tx-publisher tx-publisher))))

  (stop [this]
    (info "Stopping transaction report publisher")
    (if-not (:tx-publisher this) 
      this
      (do 
        (close! (:tx-publisher this))
        (dissoc this :tx-publisher)))))

(defn tx-report-publisher []
  (component/using 
    (map->TxReportPublisher {})
    [:db]))

(defrecord TopicPublisher [tx-report-publisher]
  component/Lifecycle
  (start [this]
    (info "Starting topic publisher")
    (if (:topic-publisher this)
      this
      (let [topic-publisher (chan)
            publication (pub topic-publisher #(:topic %))]
        (thread
          (while true
            (when-let [tx-report (<!! (:tx-publisher tx-report-publisher))]
              (publish-geolocations tx-report topic-publisher))))
        (assoc this :topic-publisher topic-publisher :publication publication))))

  (stop [this]
    (info "Stopping topic publisher")
    (if-not (:topic-publisher this)
      this
      (do
        (close! (:topic-publisher this))
        (dissoc this :topic-publisher :publication)))))

(defn topic-publisher []
  (component/using
    (map->TopicPublisher {})
    [:tx-report-publisher]))
