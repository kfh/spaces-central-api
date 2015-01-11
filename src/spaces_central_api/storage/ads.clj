(ns spaces-central-api.storage.ads
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- squuid [] (str (d/squuid)))

(defn- ->entity [tx-res eid]
  (d/entity (:db-after tx-res) eid)) 

(defn- resolve-tempids [tx-res eid]
  (let [tempids (:tempids tx-res)
        db-after (:db-after tx-res)]
    (d/resolve-tempid db-after tempids eid)))

(defn kw->string [kw] (clojure.string/join "/" ((juxt namespace name) kw)))

(defn- ->ad [entity]
  {:ad-id (:ad/public-id entity)
   :ad-type (kw->string (:ad/type entity))
   :ad-start-time (:ad/start-time entity)
   :ad-end-time (:ad/end-time entity)
   :ad-active (:ad/active entity)
   :res-title (-> entity :ad/real-estate :real-estate/title)
   :res-desc (-> entity :ad/real-estate :real-estate/description)
   :res-type (kw->string (-> entity :ad/real-estate :real-estate/type))
   :res-cost (-> entity :ad/real-estate :real-estate/cost)
   :res-size (-> entity :ad/real-estate  :real-estate/size)
   :res-bedrooms (-> entity :ad/real-estate :real-estate/bedrooms)
   :res-features (mapv kw->string (-> entity :ad/real-estate :real-estate/features))
   :loc-name (-> entity :ad/real-estate :real-estate/location :location/name) 
   :loc-street (-> entity :ad/real-estate :real-estate/location :location/street)  
   :loc-street-num (-> entity :ad/real-estate :real-estate/location :location/street-number)
   :loc-zip-code (-> entity :ad/real-estate :real-estate/location :location/zip-code)  
   :loc-city (-> entity :ad/real-estate :real-estate/location :location/city)    
   :geo-lat (-> entity :ad/real-estate :real-estate/location :location/geocode :geocode/latitude)  
   :geo-long (-> entity :ad/real-estate :real-estate/location :location/geocode :geocode/longitude)})    

(defn get-ad [conn ad-id]
  (let [ref [:ad/public-id ad-id]] 
    (when-let [entity (d/entity (d/db conn) ref)]
      (->ad entity))))

(defn get-ads [conn]
  (let [db (d/db conn)]
    (->> (d/q '[:find ?e
                :where [?e :ad/public-id]]
              db)
         (map #(->> (first %) (d/entity db) (->ad))))))

(defn- ->geocode-fact [attrs]
  {:geocode/latitude (:geo-lat attrs)
   :geocode/longitude (:geo-long attrs)})

(defn- ->location-fact [attrs] 
  {:location/name (:loc-name attrs)
   :location/street (:loc-street attrs)
   :location/street-number (:loc-street-num attrs)
   :location/zip-code (:loc-zip-code attrs)
   :location/city (:loc-city attrs)
   :location/geocode (->geocode-fact attrs)})

(defn- ->real-estate-fact [attrs]
  {:real-estate/title (:res-title attrs)
   :real-estate/description (:res-desc attrs)
   :real-estate/type (:res-type attrs)
   :real-estate/cost (:res-cost attrs)
   :real-estate/size (:res-size attrs)
   :real-estate/bedrooms (:res-bedrooms attrs)
   :real-estate/features (:res-features attrs)
   :real-estate/location (->location-fact attrs)})

(defn- ->ad-fact [ref attrs]
  {:db/id ref
   :ad/public-id (:ad-id attrs)
   :ad/type (:ad-type attrs)
   :ad/start-time (:ad-start-time attrs)
   :ad/end-time (:ad-end-time attrs)
   :ad/active (:ad-active attrs)
   :ad/real-estate (->real-estate-fact attrs)})

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
  (upsert-ad conn (assoc attrs :ad-id (squuid))))

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
