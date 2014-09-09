(ns spaces-central-api.service.geocodes
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.gateway.geocoder :as geocoder]  
            [spaces-central-api.storage.locations :as loc-storage]))

(timbre/refer-timbre)

(defn geocode-address [geocoder address]
  (->> address (geocoder/geocode-address geocoder) (first)))

(defn geocode-location [geocoder location]
  (geocoder/geocode-location geocoder location))

(defn find-location [conn location]
  (loc-storage/find-location conn location))
