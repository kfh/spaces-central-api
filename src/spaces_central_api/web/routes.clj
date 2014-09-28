(ns spaces-central-api.web.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [liberator.core :refer [resource]]
            [com.stuartsierra.component :as component] 
            [spaces-central-api.service.ads :as service])) 

(timbre/refer-timbre)

(defn- get-ad [db ad-id] 
  (service/get-ad (:conn db) ad-id))

(defn- get-ads [db]
  (service/get-ads (:conn db)))

(defn- create-ad [db geocoder req]
  (service/create-ad (:conn db) (:type geocoder) (:params req)))

(defn- update-ad [db geocoder ad-id req]
  (service/update-ad (:conn db) (:type geocoder) ad-id (:params req)))

(defn- delete-ad [db ad-id]
  (service/delete-ad (:conn db) ad-id))

(defrecord ApiRoutes [datomic geocoder]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (let [api-routes 
            (context "/api" []
                     (ANY "/ads" []
                          (resource
                            :allowed-methods [:get :post]
                            :available-media-types ["application/json"]
                            :handle-ok (fn [_] (get-ads datomic))      
                            :post! (fn [ctx] {::res (create-ad datomic geocoder (:request ctx))})
                            :handle-created ::res
                            :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))})))
                     (ANY "/ads/:id" [id] 
                          (resource
                            :allowed-methods [:get :put :delete]
                            :available-media-types ["application/json"]
                            :exists? (fn [_] (when-let [ad (get-ad datomic id)] {::res ad}))
                            :handle-ok ::res
                            :put! (fn [ctx] {::res (update-ad datomic geocoder id (:request ctx))})
                            :handle-created ::res
                            :delete! (fn [_] (delete-ad datomic id))
                            :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))))]
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
