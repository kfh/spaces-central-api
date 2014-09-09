(ns spaces-central-api.service.ads
  (:require [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as storage]  
            [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (storage/get-ad conn ad-id))

(defn get-ads [conn]
  (storage/get-ads conn))

(defn- add-geocodes [geocoder params]
  (if-let [geocodes (->> params (geocodes/geocode-address geocoder))]
    (let [lat (-> geocodes :geometry :location :lat) 
          long (-> geocodes :geometry :location :lng)]
      (assoc params :geo-lat lat :geo-long long))
    params))

(defn create-ad [conn geocoder params]
  (if-let [loc (geocodes/find-location 
                 conn (select-keys params [:loc-street :loc-street-num :loc-zip-code :loc-city]))]
    (let [{:keys [geo-lat geo-long]} loc]
      (-> (storage/create-ad conn (assoc params :geo-lat geo-lat :geo-long geo-long))
          (dissoc :geo-lat :geo-long)))
    (-> (storage/create-ad conn (add-geocodes geocoder params))
        (dissoc :geo-lat :geo-long))))

(defn update-ad [conn geocoder params ad-id]
  (if-let [loc (geocodes/find-location 
                 conn (select-keys params [:loc-street :loc-street-num :loc-zip-code :loc-city]))]
    (let [{:keys [geo-lat geo-long]} loc]
      (-> (storage/update-ad conn (assoc params :ad-id ad-id :geo-lat geo-lat :geo-long geo-long))
        (dissoc :geo-lat :geo-long)))
    (-> (storage/update-ad conn (add-geocodes geocoder params))
        (dissoc :geo-lat :geo-long))))

(defn delete-ad [conn ad-id]
  (storage/delete-ad conn ad-id))
