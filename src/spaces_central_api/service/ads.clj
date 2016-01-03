(ns spaces-central-api.service.ads
  (:require [ribol.core :refer [raise]]
            [taoensso.timbre :as timbre]
            [hara.data :refer [dissoc-in]]
            [spaces-central-api.storage.ads :as storage]
            [spaces-central-api.service.geocodes :as geocodes]))    

(timbre/refer-timbre)

(defn get-ad [conn id]
  (when-let [ad (storage/get-ad conn id)]
      (dissoc-in ad [:ad/real-estate :real-estate/location :location/geocode])))

(defn get-ads [conn]
  (->> (storage/get-ads conn)
       (map
         (fn [ad]
           (dissoc-in ad [:ad/real-estate :real-estate/location :location/geocode])))))

  (defn- add-geocodes [geocoder ad]
    (let [location (-> ad :ad/real-estate :real-estate/location)]
      (if-let [geo (->> location (geocodes/geocode-address geocoder))]
        (let [lat (-> geo :geometry :location :lat)
              long (-> geo :geometry :location :lng)
              geocode {:geocode/latitude lat :geocode/longitude long}]
          (assoc-in ad [:ad/real-estate :real-estate/location :location/geocode] geocode))
        (raise [:add-geocodes {:value ad}]))))

  (defn create-ad [conn geocoder ad]
    (let [create-ad (partial storage/create-ad conn)
          add-geocodes (partial add-geocodes geocoder)
          location (-> ad :ad/real-estate :real-estate/location)]
      (if-let [geocode (geocodes/find-geocode conn location)]
        (-> ad
            (assoc-in [:ad/real-estate :real-estate/location :location/geocode] geocode)
            (create-ad)
            (dissoc-in [:ad/real-estate :real-estate/location :location/geocode]))
        (-> ad
            (add-geocodes)
            (create-ad)
            (dissoc-in [:ad/real-estate :real-estate/location :location/geocode])))))

  (defn update-ad [conn geocoder ad]
    (let [update-ad (partial storage/update-ad conn)
          add-geocodes (partial add-geocodes geocoder)
          location (-> ad :ad/real-estate :real-estate/location)]
      (if-let [geocode (geocodes/find-geocode conn location)]
        (-> ad
            (assoc-in [:ad/real-estate :real-estate/location :location/geocode] geocode)
            (update-ad)
            (dissoc-in [:ad/real-estate :real-estate/location :location/geocode]))
        (-> ad
            (add-geocodes)
            (update-ad)
            (dissoc-in [:ad/real-estate :real-estate/location :location/geocode])))))

  (defn delete-ad [conn id]
    (storage/delete-ad conn id))
