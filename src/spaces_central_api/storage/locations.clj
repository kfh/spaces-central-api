(ns spaces-central-api.storage.locations
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre])) 

(timbre/refer-timbre)

(defn- ->location [entity]
  {:loc-name (:location/name entity)
   :loc-street (:location/street entity)
   :loc-street-num (:location/street-number entity)
   :loc-zip-code (:location/zip-code entity)
   :loc-city (:location/city entity)
   :geo-lat (-> entity :location/geocode :geocode/latitude)
   :geo-long (-> entity :location/geocode :geocode/longitude)})

(defn- find-eid [conn attrs]
  (let [{:keys [loc-street loc-street-num loc-zip-code loc-city]} attrs]
    (let [db (d/db conn)]
      (-> (d/q
            '[:find ?e
              :in $ ?street ?street-num ?zip-code ?city
              :where 
              [?e :location/street ?street]
              [?e :location/street-number ?street-num]
              [?e :location/zip-code ?zip-code]
              [?e :location/city ?city]]
            db
            loc-street
            loc-street-num
            loc-zip-code
            loc-city)
          (ffirst))))) 

(defn find-location [conn attrs]
  (when-let [eid (find-eid conn attrs)]
    (-> (d/entity (d/db conn) eid)
        (->location))))
