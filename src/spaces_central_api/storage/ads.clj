(ns spaces-central-api.storage.ads
  (:require [taoensso.timbre :as timbre]
            [datomic.api :as d]))

(defn get-ad [conn user-id ad-id]
  (str "Return one ad from storage"))

(defn get-ads [conn user-id]
  (str "Return many ads from storage"))
