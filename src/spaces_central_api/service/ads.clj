(ns spaces-central-api.service.ads
  (:require [ribol.core :refer [raise]]
            [taoensso.timbre :as timbre]
            [hara.data :refer [dissoc-in]]
            [spaces-central-api.domain.ads :as domain]  
            [spaces-central-api.storage.ads :as storage]  
            [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn ad-id]
  (storage/get-ad conn (domain/validate-ad-id ad-id)))

(defn get-ads [conn]
  (storage/get-ads conn))

(defn- add-geocodes [geocoder ad]
  (let [location (-> ad :ad/real-estate :real-estate/location)]
    (if-let [ geo (->> location (geocodes/geocode-address geocoder))]
      (let [lat (-> geo :geometry :location :lat) 
            long (-> geo :geometry :location :lng)
            geocode {:geocode/latitude lat :geocode/longitude long}]
        (assoc-in ad [:ad/real-estate :real-estate/location :location/geocode] geocode))
      (raise [:add-geocodes {:value ad}]))))

(defn create-ad [conn geocoder ad]
  (let [create-ad (partial storage/create-ad conn)
        add-geocodes (partial add-geocodes geocoder) 
        val-ad (domain/validate-ad ad)
        location (-> val-ad :ad/real-estate :real-estate/location)]
    (if-let [geocode (geocodes/find-geocode conn location)]   
      (-> val-ad 
          (assoc-in [:ad/real-estate :real-estate/location :location/geocode] geocode) 
          (create-ad)
          (dissoc-in [:ad/real-estate :real-estate/location :location/geocode]))
      (-> val-ad
          (add-geocodes)
          (create-ad)
          (dissoc-in [:ad/real-estate :real-estate/location :location/geocode])))))

(defn update-ad [conn geocoder ad-id ad]
  (domain/validate-ad-id ad-id)
  (let [update-ad (partial storage/update-ad conn)
        add-geocodes (partial add-geocodes geocoder)
        val-ad (domain/validate-ad ad)
        location (-> val-ad :ad/real-estate :real-estate/location)] 
    (if-let [geocode (geocodes/find-geocode conn location)]
      (-> val-ad
          (assoc-in [:ad/real-estate :real-estate/location :location/geocode] geocode) 
          (update-ad)
          (dissoc-in [:ad/real-estate :real-estate/location :location/geocode]))
      (-> val-ad
          (assoc :ad/public-id ad-id)
          (add-geocodes)  
          (update-ad)
          (dissoc-in [:ad/real-estate :real-estate/location :location/geocode])))))

(defn delete-ad [conn ad-id]
  (storage/delete-ad conn (domain/validate-ad-id ad-id)))
