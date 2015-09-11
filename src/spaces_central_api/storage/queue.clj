(ns spaces-central-api.storage.queue
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [immutant.messaging :refer [topic publish]]
            [clojure.core.async :refer [<!! thread chan close!]]))

(timbre/refer-timbre)

(defrecord HornetQGeocodes []
  component/Lifecycle

  (start [this]
    (info "Starting HornetQ(geocodes)")
    (if (:pub-in this)
      this
      (let [pub-in (chan)
            t (topic "geocodes")] 
        (thread
          (while true
            (when-let [data (<!! pub-in)]
              (publish t data :encoding :none))))
        (assoc this :pub-in pub-in))))

  (stop [this]
    (info "Stopping HornetQ(geocodes)")
    (if-not (:pub-in this)
      this
      (dissoc this :pub-in))))

(defn hornetq-geocodes []
  (map->HornetQGeocodes {}))
