(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as storage]  
            [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (storage/get-ad conn ad-id))

(defn get-ads [conn]
  (storage/get-ads conn))

(defn- add-geocodes [geocoder ad]
  (if-let [geocodes (->> ad (geocodes/geocode-address geocoder))]
    (let [lat (-> geocodes :geometry :location :lat) 
          long (-> geocodes :geometry :location :lng)]
      (assoc ad :geo-lat lat :geo-long long))
    ad))

(defn create-ad [conn geocoder ad]
  (if-let [loc (geocodes/find-location 
                 conn (select-keys ad [:loc-street :loc-street-num :loc-zip-code :loc-city]))]
    (let [{:keys [geo-lat geo-long]} loc]
      (-> (storage/create-ad conn (assoc ad :geo-lat geo-lat :geo-long geo-long))
          (dissoc :geo-lat :geo-long)))
    (-> (storage/create-ad conn (add-geocodes geocoder ad))
        (dissoc :geo-lat :geo-long))))

(defn update-ad [conn geocoder ad-id ad]
  (if-let [loc (geocodes/find-location 
                 conn (select-keys ad [:loc-street :loc-street-num :loc-zip-code :loc-city]))]
    (let [{:keys [geo-lat geo-long]} loc]
      (-> (storage/update-ad conn (assoc ad :ad-id ad-id :geo-lat geo-lat :geo-long geo-long))
          (dissoc :geo-lat :geo-long)))
    (-> (storage/update-ad conn (add-geocodes geocoder ad))
        (dissoc :geo-lat :geo-long))))

(defn delete-ad [conn ad-id]
  (storage/delete-ad conn ad-id))
