(ns spaces-central-api.gateway.geocoder
  (:require [taoensso.timbre :as timbre]  
            [geocoder.bing :as bing]
            [geocoder.google :as google]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(defrecord Google []
  component/Lifecycle

  (start [this] 
    (info "Enabling geocoder")
    (if (:type this)
      this
      (assoc this :type :google)))

  (stop [this] 
    (info "Disabling geocoder")
    (if-not (:type this)
      this
      (dissoc this :type))))

(defn google []
  (map->Google {}))

(defrecord Bing []
  component/Lifecycle

  (start [this] 
    (info "Enabling geocoder")
    (if (:type this)
      this
      (assoc this :type :bing)))

  (stop [this] 
    (info "Disabling geocoder")
    (if-not (:type this)
      this
      (dissoc this :type))))

(defn bing []
  (map->Bing {}))

(defmulti geocode-address (fn [type address] type))

(defmulti geocode-location (fn [type location] type))

(defmethod geocode-address :google [_ address]
 (let [{:keys [:location/street :location/street-number :location/zip-code :location/city]} address]
  (google/geocode-address (str street  " " street-number ", " zip-code " " city))))

(defmethod geocode-address :bing [_ address]
 (let [{:keys [:location/street :location/street-number :location/zip-code :location/city]} address]
  (bing/geocode-address (str street  " " street-number ", " zip-code " " city))))

(defmethod geocode-location :google [_ location]
  (google/geocode-location (str (:lat location) "," (:long location))))

(defmethod geocode-location :bing [_ location]
  (bing/geocode-location (str (:lat location) "," (:long location))))
