(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as ad-storage]))  

(defn get-ad [db user-id ad-id]
  (ad-storage/get-ad (:conn db) user-id ad-id))

(defn get-ads [db user-id]
  (ad-storage/get-ads (:conn db) user-id))
