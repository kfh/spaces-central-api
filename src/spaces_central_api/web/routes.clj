(ns spaces-central-api.web.routes
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [spaces-central-api.service.ads :as ad-service])) 

(timbre/refer-timbre)

(defn- get-ad [db ad-id] 
  (if-let [ad (ad-service/get-ad (:conn db) ad-id)]
    {:status 200 :body ad}
    {:status 404}))

(defn- get-ads [db]
  (if-let [ads (ad-service/get-ads (:conn db))]
    {:status 200 :body ads}
    {:status 404}))

(defn- create-ad [db req]
  {:status 201
   :body (ad-service/create-ad 
           (:conn db) 
           (select-keys 
             (:params req) 
             [:type :start-time :end-time :active]))})

(defn- update-ad [db req ad-id]
  {:status 201
   :body (ad-service/update-ad 
           (:conn db) 
           (select-keys 
             (:params req) 
             [:type :start-time :end-time :active])
          ad-id)})

(defn- delete-ad [db ad-id]
  (if (ad-service/delete-ad (:conn db) ad-id)
    {:status 204}
    {:status 404}))

(defrecord ApiRoutes [datomic geocoder]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (let [api-routes (routes
                         (GET "/ads" req (get-ads datomic))
                         (GET "/ads/:ad-id" [ad-id :as req] (get-ad datomic ad-id))
                         (POST "/ads" req (create-ad datomic req))
                         (PUT "/ads/:ad-id" [ad-id :as req] (update-ad datomic req ad-id))
                         (DELETE "/ads/:ad-id" [ad-id :as req] (delete-ad datomic ad-id))
                         (route/resources "/")
                         (route/not-found "Not Found"))]
        (assoc this :routes api-routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:datomic :geocoder]))
