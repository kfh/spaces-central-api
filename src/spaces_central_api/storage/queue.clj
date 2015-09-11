(ns spaces-central-api.storage.queue
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [immutant.messaging :refer [topic publish]]
            [clojure.core.async :refer [<!! thread chan close!]]))

(timbre/refer-timbre)

(defrecord HornetQGeolocations []
  component/Lifecycle

  (start [this]
    (info "Starting HornetQ(geolocations)")
    (if (:pub-in this)
      this
      (let [pub-in (chan)
            t (topic "geolocations")] 
        (thread
          (while true
            (when-let [data (<!! pub-in)]
              (publish t data :encoding :none))))
        (assoc this :pub-in pub-in))))

  (stop [this]
    (info "Stopping HornetQ(geolocations)")
    (if-not (:pub-in this)
      this
      (dissoc this :pub-in))))

(defn hornetq-geolocations []
  (map->HornetQGeolocations {}))
