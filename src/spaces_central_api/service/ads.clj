(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as ad-storage]))  

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (ad-storage/get-ad conn ad-id))

(defn get-ads [conn]
  (ad-storage/get-ads conn))

(defn create-ad [conn params]
  (ad-storage/create-ad conn params))

(defn update-ad [conn params ad-id]
  (ad-storage/update-ad conn (assoc params :id ad-id)))

(defn delete-ad [conn ad-id]
  (ad-storage/delete-ad conn ad-id))
