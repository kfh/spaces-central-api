(ns spaces-central-api.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defrecord ChannelSockets [handler]
  component/Lifecycle
  
  (start [this]
    (info "Starting channel sockets")
    (if (:router this)
      this
      (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
            (sente/make-channel-socket! {})]
        (assoc this 
               :ring-ajax-post ajax-post-fn 
               :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
               :ch-chsk ch-recv
               :chsk-send! send-fn
               :connected-uids connected-uids
               :router (atom (sente/start-chsk-router! ch-recv handler))))))

  (stop [this]
    (info "Stopping channel sockets")
    (if-not (:router this)
      this
      (let [stop @(:router this)]
        (stop)
        (dissoc this :router)))))

(defn channel-sockets []
  (component/using
    (map->ChannelSockets {})
    [:ring-handler]))
