(ns spaces-central-api.storage.subscriber
  (:require [datomic.api :as d]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! sub unsub-all chan put! close! go-loop]])
  (import [java.io ByteArrayOutputStream]))

(timbre/refer-timbre)

(defn- write-transit [data]
  (let [buffer (ByteArrayOutputStream. 4096)
        writer (transit/writer buffer :json)]
    (transit/write writer data)
    (.toByteArray buffer)))

(defn- queue-geolocations [channel geolocations]
  (put! channel (write-transit geolocations)))

(defn- take-and-queue-geolocations [channel subscriber]
  (go-loop []
    (when-let [geolocations (<! subscriber)]
      (queue-geolocations channel geolocations)) 
    (recur)))

(defrecord TxReportSubscriber [publisher zeromq]
  component/Lifecycle

  (start [this]
    (info "Starting transaction report subscriber")
    (if (:subscriber this)
      this
      (let [subscriber (chan)
            subscription (sub (:publication publisher) :geolocations subscriber)]
        (take-and-queue-geolocations (:pub-channel zeromq) subscriber)
        (assoc this :subscriber subscriber :subscription subscription))))

  (stop [this]
    (info "Stopping transaction report subscriber")
    (if-not (:subscriber this)
      this
      (do 
        (unsub-all (:publication publisher))
        (close! (:subscriber this))
        (dissoc this :subscriber :subscription)))))

(defn tx-report-subscriber []
  (component/using
    (map->TxReportSubscriber {})
    [:publisher :zeromq]))
