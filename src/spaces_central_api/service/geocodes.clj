(ns spaces-central-api.service.geocodes
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.geocodes :as storage]
            [spaces-central-api.gateway.geocoder :as geocoder]))

(timbre/refer-timbre)

(defn geocode-address [geocoder address]
  (geocoder/geocode-address geocoder address))

(defn geocode-location [geocoder location]
  (geocoder/geocode-location geocoder location))

(defn find-geocode-by-address [conn address]
  (storage/find-geocode conn address))

(defn find-location-by-geocode [conn geocode]
  (storage/find-location conn geocode))
