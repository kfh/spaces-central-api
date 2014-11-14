(ns spaces-central-api.storage.listener
  (:require [datomic.api :as d]
            [clj-http.client :as http]
            [taoensso.timbre :as timbre]
            [ribol.core :refer [manage]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! chan tap untap go-loop]]))

(timbre/refer-timbre)

(defn- store-location [search-api-url location] 
  (let [post (partial http/post (str search-api-url "/api/locations"))] 
    (manage 
      (let [id (:id location)
            res (post {:form-params location :content-type :transit+json :as :transit+json})]
        (if (= 201 (:status res))
          (info "Storage of location(" id ") in external system succeeded")
          (warn "Storage of location(" id ") in external system failed")))
      (catch Exception ex
        (warn "External storage of location failed: " (.getMessage ex))))))

(defn- delete-location [search-api-url location] 
  (manage 
    (let [id (:id location)
          res (http/delete (str search-api-url "/api/locations/" id))]
      (if (= 204 (:status res))
        (info "Deletion of location(" id ") from external system succeeded")
        (warn "Deletion of location(" id ") from external system failed")))
    (catch Exception ex
      (warn "External deletion of location failed: " (.getMessage ex)))))

(defn- contains-keys? [m keys]
  (apply = (map count [keys (select-keys m keys)])))

(defn- process-txes [search-api-url attrs]
  (when (contains-keys? attrs [:ad/public-id :geocode/latitude :geocode/longitude])
    (let [geocodes {:lat (:geocode/latitude attrs) :lon (:geocode/longitude attrs)}
          location {:id (:ad/public-id attrs) :geocodes geocodes}]
      (if (:added? attrs)
        (store-location search-api-url location)
        (delete-location search-api-url location)))))

(defn ->attr-data [txes]
  (->> (d/q 
         '[:find ?aname ?v ?added
           :in $ [[?e ?a ?v _ ?added]]
           :where 
           [?e ?a ?v _ ?added]
           [?a :db/ident ?aname]]
         (:db-after txes)
         (:tx-data txes))
       (map #(let [[aname val added] %] {aname val :added? added})) 
       (apply merge)))

(defn- take-and-process-txes [search-api-url txes]
  (go-loop []
     (process-txes search-api-url (->attr-data (<! txes))) 
    (recur)))

(defrecord TxListener [env watcher]
  component/Lifecycle
  
  (start [this]
    (info "Starting transaction listener")
    (if (:tx-tap this)
      this
      (let [txes (chan)
            tx-tap(tap (:tx-listener watcher) txes)]
        (take-and-process-txes (:search-api-url env) txes)
        (assoc this :tx-tap tx-tap))))
  
  (stop [this]
    (info "Stopping transaction listener")
    (if-not (:tx-tap this)
      this
      (do 
        (untap (:tx-listener watcher) (:tx-tap this))
        (dissoc this :tx-tap)))))

(defn tx-listener []
  (component/using
    (map->TxListener {})
    [:env :watcher]))
