(ns spaces-central-api.gateway.geolocation
  (:require [org.httpkit.client :as http]
            [cheshire.core :refer [parse-string]]
            [ring.util.codec :refer [url-encode]]
            [clojure.set :refer [rename-keys]]
            [com.stuartsierra.component :as component]))

(defrecord Geolocation [baseurl]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn geolocation [baseurl]
  (map->Geolocation {:baseurl baseurl}))

(defn- extract-coordinates [result]
  (rename-keys (get-in result ["geometry" "location"])
               {"lat" :latitude "lng" :longitude}))

(defn- extract-address [result]
  (get result "formatted_address"))

(defn- parse-response [{:keys [status body headers] :as response}]
  (if (= status 200)
    (let [results (-> body parse-string (get "results"))]
      (map (fn [result]
             {:address (extract-address result)
              :coordinates (extract-coordinates result)}) results))
    (throw (Exception. (str "Ooops:" body)))))

(defn- geo-request [geo params]
  (let [baseurl (:baseurl geo) #_"https://maps.googleapis.com/maps/api/geocode/json?"
        url (str (:baseurl geo) params)
        resp  @(http/get url)]
    (parse-response resp)))

(defn search-by-address [geo address]
  (geo-request geo (str "address=" (url-encode address))))

(defn search-by-coordinates [geo latitude longitude]
  (geo-request geo (str "latlng=" latitude "," longitude)))

