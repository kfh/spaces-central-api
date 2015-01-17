(ns spaces-central-api.service.geocodes
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.geocodes :as storage]
            [spaces-central-api.gateway.geocoder :as geocoder]))

(timbre/refer-timbre)

(defn geocode-address [geocoder address]
  (->> address (geocoder/geocode-address geocoder) (first)))

(defn geocode-location [geocoder location]
  (geocoder/geocode-location geocoder location))

(defn find-geocode [conn ad]
  (storage/find-geocode conn ad))
