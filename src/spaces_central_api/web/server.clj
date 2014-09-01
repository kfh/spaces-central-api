(ns spaces-central-api.web.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defrecord WebServer [port ring-handler]
  component/Lifecycle

  (start [this]
    (info "Starting web server")
    (if (:server this) 
      this
      (assoc this :server
             (run-jetty (:handler ring-handler) {:port port :join? false}))))

  (stop [this]
    (info "Stopping web server")
    (if-not (:server this) 
      this
      (do (.stop (:server this))
          (dissoc this :server)))))

(defn web-server [port]
  (component/using 
    (map->WebServer {:port port})
    [:ring-handler]))
