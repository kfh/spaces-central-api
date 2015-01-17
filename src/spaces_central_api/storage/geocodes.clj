(ns spaces-central-api.storage.geocodes
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- find-eid [conn location]
  (let [db (d/db conn)
        {:keys [location/name location/street location/city 
                location/zip-code location/street-number]} location]
    (-> (d/q
          '[:find ?eid .
            :in $ [?name ?street ?number ?zip ?city]
            :where 
            [?location :location/name ?name]
            [?location :location/street ?street]
            [?location :location/street-number ?number]
            [?location :location/zip-code ?zip]
            [?location :location/city ?city]
            [?location :location/geocode ?eid]]
          db
          [name street street-number zip-code city])))) 

(defn- ->geocode [entity]
  {:geocode/latitude (:geocode/latitude entity)
   :geocode/longitude (:geocode/longitude entity)})

(defn find-geocode [conn location]
  (when-let [eid (find-eid conn location)]
    (-> (d/entity (d/db conn) eid)
        (->geocode))))
