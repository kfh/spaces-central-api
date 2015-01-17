(ns spaces-central-api.service.ads
    (:require [ribol.core :refer [raise]]
              [taoensso.timbre :as timbre]
              [spaces-central-api.domain.ads :as domain]  
              [spaces-central-api.storage.ads :as storage]  
              [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (storage/get-ad conn (domain/validate-ad-id ad-id)))

(defn get-ads [conn]
  (storage/get-ads conn))

(defn- add-geocodes [geocoder ad]
  (if-let [geocodes (->> ad (geocodes/geocode-address geocoder))]
    (let [lat (-> geocodes :geometry :location :lat) 
          long (-> geocodes :geometry :location :lng)]
      (assoc ad :geo-lat lat :geo-long long))
    (raise [:add-geocodes {:value ad}])))

(defn create-ad [conn geocoder ad]
  (let [create-ad (partial storage/create-ad conn)
        add-geocodes (partial add-geocodes geocoder) 
        val-ad (->> ad (domain/coerce-ad) (domain/validate-ad))]
    (if-let [loc (geocodes/find-geocode conn val-ad)]   
      (let [{:keys [geo-lat geo-long]} loc] 
        (-> val-ad 
            (assoc :geo-lat geo-lat :geo-long geo-long)
            (create-ad)
            (dissoc ad :geo-lat :geo-long)))
      (-> val-ad
          (add-geocodes)  
          (create-ad)
          (dissoc ad :geo-lat :geo-long)))))

(defn update-ad [conn geocoder ad-id ad]
  (domain/validate-ad-id ad-id)
  (let [update-ad (partial storage/update-ad conn)
        add-geocodes (partial add-geocodes geocoder)
        val-ad (->> ad (domain/coerce-ad) (domain/validate-ad))] 
    (if-let [loc (geocodes/find-geocode conn val-ad)]
      (let [{:keys [geo-lat geo-long]} loc] 
        (-> val-ad
            (assoc :ad-id ad-id :geo-lat geo-lat :geo-long geo-long)  
            (update-ad)
            (dissoc ad :geo-lat :geo-long)))
      (-> val-ad
          (assoc :ad-id ad-id)
          (add-geocodes)  
          (update-ad)
          (dissoc ad :geo-lat :geo-long)))))

(defn delete-ad [conn ad-id]
  (storage/delete-ad conn (domain/validate-ad-id ad-id)))
