(ns spaces-central-api.web.routes
  (:require [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [compojure.core :refer [ANY context]] 
            [liberator.core :refer [defresource]]
            [io.clojure.liberator-transit :refer :all] 
            [com.stuartsierra.component :as component] 
            [spaces-central-api.service.ads :as service])) 

(timbre/refer-timbre)

(defn- get-ad [db ad-id] 
  (service/get-ad (:conn db) ad-id))

(defn- get-ads [db]
  (service/get-ads (:conn db)))

(defn- create-ad [env db geocoder req]
  (service/create-ad (:search-api-url env) (:conn db) (:type geocoder) (:params req)))

(defn- update-ad [env db geocoder ad-id req]
  (service/update-ad (:search-api-url env) (:conn db) (:type geocoder) ad-id (:params req)))

(defn- delete-ad [env db ad-id]
  (service/delete-ad (:search-api-url env) (:conn db) ad-id))

(defresource list-resource [env datomic geocoder]
  :allowed-methods [:get :post]
  :available-media-types ["application/transit+json"]
  :handle-ok (fn [_] (get-ads datomic))      
  :post! (fn [ctx] {::res (create-ad env datomic geocoder (:request ctx))})
  :handle-created ::res
  :as-response (as-response {:allow-json-verbose? false})
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defresource ad-resource [env datomic geocoder ad-id]
  :allowed-methods [:get :put :delete]
  :available-media-types ["application/transit+json"]
  :exists? (fn [_] (when-let [ad (get-ad datomic ad-id)] {::res ad}))
  :handle-ok ::res
  :put! (fn [ctx] {::res (update-ad env datomic geocoder ad-id (:request ctx))})
  :handle-created ::res
  :delete! (fn [_] (delete-ad env datomic ad-id))
  :as-response (as-response {:allow-json-verbose? false})
  :handle-exception (fn [ctx] {::error (.getMessage (:exception ctx))}))

(defrecord ApiRoutes [env datomic geocoder]
  component/Lifecycle

  (start [this]
    (info "Enabling api routes")
    (if (:routes this)
      this 
      (->> (context "/api" []
                    (ANY "/ads" [] (list-resource env datomic geocoder))
                    (ANY "/ads/:ad-id" [ad-id] (ad-resource env datomic geocoder ad-id) ))
           (assoc this :routes))))

  (stop [this]
    (info "Disabling api routes")
    (if-not (:routes this) 
      this
      (dissoc this :routes))))

(defn api-routes []
  (component/using
    (map->ApiRoutes {})
    [:env :datomic :geocoder]))
