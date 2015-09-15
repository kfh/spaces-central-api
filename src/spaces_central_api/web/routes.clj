(ns spaces-central-api.web.routes
  (:require [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [liberator.core :refer [defresource]]
            [io.clojure.liberator-transit :refer :all] 
            [com.stuartsierra.component :as component] 
            [compojure.core :refer [context ANY GET POST]] 
            [spaces-central-api.service.ads :as service])) 

(timbre/refer-timbre)

(defn- get-ad [db ad-id] 
  (service/get-ad (:conn db) ad-id))

(defn- get-ads [db]
  (service/get-ads (:conn db)))

(defn- create-ad [db geocoder req]
  (let [params (read-string (:transit-params req))] 
    (service/create-ad (:conn db) (:type geocoder) params)))

(defn- update-ad [db geocoder ad-id req]
  (let [params (read-string (:transit-params req))] 
    (service/update-ad (:conn db) (:type geocoder) ad-id params)))

(defn- delete-ad [db ad-id]
  (service/delete-ad (:conn db) ad-id))

(defresource list-resource [datomic geocoder]
  :allowed-methods [:get :post]
  :available-media-types ["application/transit+json"]
  :handle-ok (fn [_] (get-ads datomic))      
  :post! (fn [ctx] {::res (create-ad datomic geocoder (:request ctx))})
  :handle-created ::res
  :as-response (as-response {:allow-json-verbose? false})
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource ad-resource [datomic geocoder ad-id]
  :allowed-methods [:get :put :delete]
  :available-media-types ["application/transit+json"]
  :exists? (fn [_] (when-let [ad (get-ad datomic ad-id)] {::res ad}))
  :handle-ok ::res
  :put! (fn [ctx] {::res (update-ad datomic geocoder ad-id (:request ctx))})
  :handle-created ::res
  :delete! (fn [_] (delete-ad datomic ad-id))
  :as-response (as-response {:allow-json-verbose? false})
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defrecord ApiRoutes [datomic geocoder]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (->> (context "/api" []
                    (ANY "/ads" [] (list-resource datomic geocoder))
                    (ANY "/ads/:ad-id" [ad-id] (ad-resource datomic geocoder ad-id) ))
           (assoc this :routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:datomic :geocoder]))
