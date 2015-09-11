(ns spaces-central-api.storage.subscriber
  (:require [datomic.api :as d]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! sub unsub chan put! close! go-loop]])
  (import [java.io ByteArrayOutputStream]))

(timbre/refer-timbre)

(defn- write-transit [data]
  (let [buffer (ByteArrayOutputStream. 4096)
        writer (transit/writer buffer :json)]
    (transit/write writer data)
    (.toByteArray buffer)))

(defrecord GeolocationsSubscriber [topic-publisher hornetq-geolocations]
  component/Lifecycle
  (start [this]
    (info "Starting geolocations subscriber")
    (if (:subscriber this)
      this
      (let [subscriber (chan)
            subscription (sub (:publication topic-publisher) :geolocations subscriber)]
        (go-loop []
          (when-let [geolocations (<! (:topic-publisher topic-publisher))]
            (put! (:pub-in hornetq-geolocations) (-> (:data geolocations) (write-transit)))) 
          (recur))
        (assoc this :subscriber subscriber :subscription subscription))))

  (stop [this]
    (info "Stopping geolocations subscriber")
    (if-not (:subscriber this)
      this
      (do 
        (unsub (:publication topic-publisher) :geolocations (:subscriber this))
        (close! (:subscriber this))
        (dissoc this :subscriber :subscription)))))

(defn geolocations-subscriber []
  (component/using
    (map->GeolocationsSubscriber {})
    [:topic-publisher :hornetq-geolocations]))
