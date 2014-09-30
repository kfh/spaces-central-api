(ns spaces-central-api.domain.locations
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]))

(def Location {(s/required-key :loc-street) s/Str
               (s/required-key :loc-street-num) s/Str
               (s/required-key :loc-zip-code) s/Str
               (s/required-key :loc-city) s/Str})

(defn validate-location [location]
  (s/validate Location location))
