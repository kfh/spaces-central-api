(ns spaces-central-api.storage.publisher
  (:require [datomic.api :as d]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [>!! pub chan close! thread]]))

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

(defrecord TxReportPublisher [datomic]
  component/Lifecycle

  (start [this]
    (info "Starting transaction report publisher")
    (if (:publisher this) 
      this
      (let [publisher (chan)
            publication (pub publisher #(:topic %))
            tx-queue (d/tx-report-queue (:conn datomic))]
        (thread 
          (while true
            (when-let [tx-report (.take tx-queue)]
              (let [data (take-geolocations tx-report)]
                (when-not (empty? data)
                  (>!! publisher {:topic :geolocations :data data}))))))
        (assoc this :publisher publisher :publication publication))))

  (stop [this]
    (info "Stopping transaction report publisher")
    (if-not (:publisher this) 
      this
      (do 
        (close! (:publisher this))
        (dissoc this :publisher :publication)))))

(defn tx-report-publisher []
  (component/using 
    (map->TxReportPublisher {})
    [:datomic]))
