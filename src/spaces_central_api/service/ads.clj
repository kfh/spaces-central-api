(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as storage]  
            [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (storage/get-ad conn ad-id))

(defn get-ads [conn]
  (storage/get-ads conn))

(defn create-ad [conn geocoder params]
  (storage/create-ad conn params))

(defn update-ad [conn geocoder params ad-id]
  (storage/update-ad conn (assoc params :id ad-id)))

(defn delete-ad [conn ad-id]
  (storage/delete-ad conn ad-id))
