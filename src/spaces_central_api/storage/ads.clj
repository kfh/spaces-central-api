(ns spaces-central-api.storage.ads
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- squuid [] (str (d/squuid)))

(defn- tempids [] 
  (repeatedly #(d/tempid :db.part/user))) 

(defn- ->entity [tx-res eid]
  (->> eid
       (d/resolve-tempid 
         (:db-after tx-res) (:tempids tx-res))
       (d/entity (:db-after tx-res)))) 

(defn- ->ad [entity]
  {:id (:ad/public-id entity)
   :type (:ad/type entity)
   :start-time (:ad/start-time entity)
   :end-time (:ad/end-time entity)
   :active (:ad/active entity)})

(defn- get-eid [conn ad-id]
  (let [db (d/db conn)]
    (-> (d/q
          '[:find ?e
            :in $ ?id
            :where [?e :ad/public-id ?id]]
          db
          ad-id)
        (ffirst))))

(defn get-ad [conn ad-id]
  (when-let [eid (get-eid conn ad-id)]
    (-> (d/entity (d/db conn) eid)
        (->ad))))

(defn get-ads [conn]
  (let [db (d/db conn)]
    (->> (d/q '[:find ?e
                :where [?e :ad/public-id]]
              db)
         (map #(->> (first %) (d/entity db) (->ad))))))

(defn- upsert-ad [conn attrs]
  (let [[ad-eid] (tempids)]
    (-> @(d/transact
           conn
           [{:db/id ad-eid
             :ad/public-id (:id attrs)
             :ad/type (:type attrs)
             :ad/start-time (:start-time attrs)
             :ad/end-time (:end-time attrs)
             :ad/active (:active attrs)}])
        (->entity ad-eid)
        (->ad))))

(defn create-ad [conn attrs]
  (upsert-ad conn (assoc attrs :id (squuid))))

(defn update-ad [conn attrs]
  (upsert-ad conn attrs))

(defn delete-ad [conn ad-id]
  (if-let [eid (get-eid conn ad-id)]
    (->> @(d/transact conn [[:db.fn/retractEntity eid]])
         :tx-data
         (remove #(:added %))
         (map :e)
         (into #{}))
    (throw (ex-info "Cannot delete ad with non existing id" {:id ad-id}))))
