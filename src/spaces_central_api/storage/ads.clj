(ns spaces-central-api.storage.ads
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre]
            [clojure.walk :refer [prewalk]]))

(timbre/refer-timbre)

(defn- squuid [] (str (d/squuid)))

(defn- ->entity [tx-res eid]
  (d/entity (:db-after tx-res) eid)) 

(defn- resolve-tempids [tx-res eid]
  (let [tempids (:tempids tx-res)
        db-after (:db-after tx-res)]
    (d/resolve-tempid db-after tempids eid)))

(defn- ->ad [entity]
  (-> entity
      (d/touch)
      (pr-str)
      (read-string)
      (as-> realized-entity
        (prewalk 
          #(if (map? %) (dissoc % :db/id) %) 
          realized-entity))))    

(defn get-ad [conn ad-id]
  (let [ref [:ad/public-id ad-id]] 
    (when-let [entity (d/entity (d/db conn) ref)]
      (->ad entity))))

(defn get-ads [conn]
  (let [db (d/db conn)]
    (->> (d/q '[:find ?e
                :where [?e :ad/public-id]]
              db)
         (map 
           #(->> (first %) 
                 (d/entity db) 
                 (->ad))))))

(defn- ->geocode-fact [attrs]
  {:geocode/latitude (:geocode/latitude attrs)
   :geocode/longitude (:geocode/longitude attrs)})

(defn- ->location-fact [attrs] 
  {:location/name (:location/name attrs)
   :location/street (:location/street attrs)
   :location/street-number (:location/street-number attrs)
   :location/zip-code (:location/zip-code attrs)
   :location/city (:location/city attrs)
   :location/geocode (->geocode-fact (:location/geocode attrs))})

(defn- ->real-estate-fact [attrs]
  {:real-estate/title (:real-estate/title attrs)
   :real-estate/description (:real-estate/description attrs)
   :real-estate/type (:real-estate/type attrs)
   :real-estate/cost (:real-estate/cost attrs)
   :real-estate/size (:real-estate/size attrs)
   :real-estate/bedrooms (:real-estate/bedrooms attrs)
   :real-estate/features (:real-estate/features attrs)
   :real-estate/location (->location-fact (:real-estate/location attrs))})

(defn- ->ad-fact [ref attrs]
  {:db/id ref
   :ad/public-id (:ad/public-id attrs)
   :ad/type (:ad/type attrs)
   :ad/start-time (:ad/start-time attrs)
   :ad/end-time (:ad/end-time attrs)
   :ad/active (:ad/active attrs)
   :ad/real-estate (->real-estate-fact (:ad/real-estate attrs))})

(defn- upsert-ad [conn attrs]
  (let [ref [:ad/public-id (:ad-id attrs)]]
    (if (d/entity (d/db conn) ref)
      (-> @(d/transact
             conn
             (vector (->ad-fact ref attrs)))
          (->entity ref)
          (->ad))
      (let [tempid (d/tempid :db.part/user)
            tx-res @(d/transact conn (vector (->ad-fact tempid attrs)))]
        (->> tempid  
             (resolve-tempids tx-res)
             (->entity tx-res)
             (->ad))))))

(defn create-ad [conn attrs]
  (upsert-ad conn (assoc attrs :ad/public-id (squuid))))

(defn update-ad [conn attrs]
  (upsert-ad conn attrs))

(defn delete-ad [conn ad-id]
  (let [ref [:ad/public-id ad-id]] 
    (when-let [entity (d/entity (d/db conn) ref)]
      (->> @(d/transact conn [[:db.fn/retractEntity (:db/id entity)]])
           :tx-data
           (remove #(:added %))
           (map :e)
           (into #{})))))
