(ns spaces-central-api.storage.queue
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]  
            [com.keminglabs.zmq-async.core :refer [register-socket!]]
            [clojure.core.async :refer [chan sliding-buffer pipe close!]]))

(timbre/refer-timbre)

(def publisher "tcp://*:17778")
(def subscriber "tcp://*:17779")

(def pub-in (chan (sliding-buffer 1024)))
(def xpub-in (chan (sliding-buffer 1024)))
(def xpub-out (chan (sliding-buffer 1024)))
(def xsub-in (chan (sliding-buffer 1024)))
(def xsub-out (chan (sliding-buffer 1024)))

(def xsub {:in xsub-in
           :out xsub-out
           :socket-type :xsub
           :configurator (fn [socket] 
                           (.bind socket subscriber))})

(def xpub {:in xpub-in
           :out xpub-out
           :socket-type :xpub
           :configurator (fn [socket] 
                           (do 
                             (.setSndHWM socket 1000)
                             (.bind socket publisher)))})

(def pub {:in pub-in
          :socket-type :pub
          :configurator (fn [socket] 
                          (.connect socket subscriber))})

(defrecord ZeroMQ []
  component/Lifecycle

  (start [this]
    (info "Starting ZeroMQ publisher")
    (if (:pub-channel this)
      this
      (do 
        (register-socket! xsub)
        (register-socket! xpub)
        (register-socket! pub)
        (pipe xsub-out xpub-in)
        (pipe xpub-out xsub-in)
        (assoc this :pub-channel pub-in))))

  (stop [this]
    (info "Stopping ZeroMQ publisher")
    (if-not (:pub-channel this)
      this
      (do
        (close! xsub-in)
        (close! xpub-in)
        (close! pub-in)
        (dissoc this :pub-channel)))))

(defn zeromq []
  (map->ZeroMQ {}))
