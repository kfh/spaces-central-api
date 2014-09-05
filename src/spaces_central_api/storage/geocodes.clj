(ns spaces-central-api.storage.geocodes
  (:require [datomic.api :as d] 
            [taoensso.timbre :as timbre])) 

(timbre/refer-timbre)

(defn find-geocode [conn address])

(defn find-location [conn geocode])
