(ns spaces-central-api.web.routes
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [spaces-central-api.service.ads :as ad-service])) 

(timbre/refer-timbre)

(defn- get-ad [db req ad-id]
  (if-let [ad (ad-service/get-ad db (:user-id req) ad-id)]
    {:status 200 :body ad}
    {:status 404}))

(defn- get-ads [db req]
  (if-let [ads (ad-service/get-ads db (:user-id req))]
    {:status 200 :body ads}
    {:status 404}))

(defrecord ApiRoutes [datomic]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (let [api-routes (routes
                       (GET "/ads" req (get-ads datomic req))
                       (GET "/ads/:ad-id" [ad-id :as req] (get-ad datomic req ad-id))
                       (route/resources "/")
                       (route/not-found "Not Found"))]
      (assoc this :routes api-routes)))

  (stop [this]
    (info "Disabling api routes")
    (dissoc this :routes)))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:datomic]))
