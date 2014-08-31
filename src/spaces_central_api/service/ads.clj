(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as ad-storage]))  

(defn get-ad [conn ad-id]
  (ad-storage/get-ad conn ad-id))

(defn get-ads [conn user-id]
  (ad-storage/get-ads conn user-id))
