(ns spaces-central-api.storage.listener
  (:require [datomic.api :as d]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [ribol.core :refer [manage]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! chan tap untap go-loop]]))

(timbre/refer-timbre)

(defn- store-geolocation [search-api-url geolocation] 
  (let [post (partial http/post (str search-api-url "/api/locations"))] 
    (manage 
      (let [id (:id geolocation)
            res (post {:form-params (dissoc geolocation :added) :content-type :transit+json 
                       :as :transit+json :throw-exceptions false})]
        (if (= 201 (:status res))
          (info "Storage of location(" id ") in external system succeeded")
          (warn "Storage of location(" id ") in external system failed:" 
                {:status (:status res) :error (:body res)}))))))

(defn- delete-geolocation [search-api-url geolocation] 
  (manage 
    (let [id (:id geolocation)
          ex {:throw-exceptions false}   
          res (http/delete (str search-api-url "/api/locations/" id) ex)]
      (if (= 204 (:status res))
        (info "Deletion of location(" id ") from external system succeeded")
        (warn "Deletion of location(" id ") from external system failed:"
              {:status (:status res) :error (:body res)})))))

(defn- process-geolocations [search-api-url geolocations]
  (doseq [geolocation geolocations] 
    (if (:added geolocation)
      (store-geolocation search-api-url geolocation)
      (delete-geolocation search-api-url geolocation))))

(defn- ->geolocations [tx-report] 
  (keep 
    (fn [[e a v t added]]
      (let [db (if added (:db-after tx-report) (:db-before tx-report))
            entity (partial d/entity db)] 
        (when (= :location/geocode (-> a entity :db/ident))
          (let [{:keys [:geocode/latitude :geocode/longitude]} (entity v)
                public-id '[{:real-estate/_location [{:ad/_real-estate [:ad/public-id]}]}]]
            {:id (->> e
                      (d/pull db public-id) 
                      :real-estate/_location 
                      :ad/_real-estate 
                      :ad/public-id) 
             :geocodes {:lat latitude :lon longitude}
             :_timestamp (->> t 
                              (d/entity (:db-after tx-report)) 
                              :db/txInstant) 
             :added added})))) 
    (:tx-data tx-report)))

(defn- take-and-process-geolocations [search-api-url tx-report]
  (go-loop []
     (process-geolocations search-api-url (->geolocations (<! tx-report))) 
    (recur)))

(defrecord TxListener [env watcher]
  component/Lifecycle
  
  (start [this]
    (info "Starting transaction report listener")
    (if (:tx-tap this)
      this
      (let [tx-report (chan)
            tx-report-tap(tap (:tx-listener watcher) tx-report)]
        (take-and-process-geolocations (:search-api-url env) tx-report)
        (assoc this :tx-tap tx-report-tap))))
  
  (stop [this]
    (info "Stopping transaction report listener")
    (if-not (:tx-tap this)
      this
      (do 
        (untap (:tx-listener watcher) (:tx-tap this))
        (dissoc this :tx-tap)))))

(defn tx-listener []
  (component/using
    (map->TxListener {})
    [:env :watcher]))
