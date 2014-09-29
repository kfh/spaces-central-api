(ns spaces-central-api.domain.ads
  (:require [schema.core :as s]
            [schema.coerce :as coerce]
            [taoensso.timbre :as timbre]))

(def ad-types (s/enum :ad.type/real-estate))

(def res-types (s/enum :real-estate.type/apartment :real-estate.type/house)) 

(def features (s/enum :real-estate.feature/elevator :real-estate.feature/aircondition 
                      :real-estate.feature/fireplace :real-estate.feature/lawn 
                      :real-estate.feature/garage)) 

(def Ad {(s/optional-key :ad-id) s/Str
         (s/required-key :ad-type) ad-types
         (s/optional-key :ad-start-time) s/Str
         (s/optional-key :ad-end-time) s/Str
         (s/required-key :ad-active) boolean
         (s/optional-key :res-title) s/Str
         (s/optional-key :res-desc) s/Str
         (s/required-key :res-type) res-types 
         (s/optional-key :res-cost) s/Str
         (s/optional-key :res-size) s/Str
         (s/optional-key :res-bedrooms) s/Str
         (s/required-key :res-features) [features] 
         (s/optional-key :loc-name) s/Str
         (s/optional-key :loc-street) s/Str
         (s/optional-key :loc-street-num) s/Str
         (s/optional-key :loc-zip-code) s/Str
         (s/optional-key :loc-city) s/Str})

(def coerce-ad (coerce/coercer Ad coerce/json-coercion-matcher))

(def RealEstateAd 
  {(s/optional-key :ad-id) s/Str
   (s/required-key :ad-type) (s/eq :ad.type/real-estate)
   (s/required-key :ad-start-time) s/Str
   (s/required-key :ad-end-time) s/Str
   (s/required-key :ad-active) boolean
   (s/required-key :res-title) s/Str
   (s/required-key :res-desc) s/Str
   (s/required-key :res-type) res-types
   (s/required-key :res-cost) s/Str
   (s/required-key :res-size) s/Str
   (s/required-key :res-bedrooms) s/Str
   (s/required-key :res-features) [features]
   (s/required-key :loc-name) s/Str
   (s/required-key :loc-street) s/Str
   (s/required-key :loc-street-num) s/Str
   (s/required-key :loc-zip-code) s/Str
   (s/required-key :loc-city) s/Str})

(defn validate-ad [ad]
  (s/validate RealEstateAd ad))

(defn validate-ad-id [ad-id]
  (s/validate s/Str ad-id))
