(ns spaces-central-api.web.handler
   (:require [com.stuartsierra.component :as component]
             [compojure.handler :as handler]
             [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defrecord RingHandler [api-routes]
  component/Lifecycle
  
  (start [this]
    (info "Enabling ring handler")
    (assoc this :handler (-> (:routes api-routes) handler/api)))

  (stop [this]
    (info "Disabling ring handler")
    (dissoc this :handler)))

(defn ring-handler []
  (component/using 
    (map->RingHandler {})
    [:api-routes]))
