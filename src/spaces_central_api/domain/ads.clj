(ns spaces-central-api.domain.ads
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]))

(def res-types (s/enum :real-estate.type/apartment :real-estate.type/house)) 

(def features [(s/enum :real-estate.feature/elevator :real-estate.feature/aircondition 
                       :real-estate.feature/fireplace :real-estate.feature/lawn 
                       :real-estate.feature/garage)]) 

(def Location
  {(s/required-key :location/name) s/Str
   (s/required-key :location/street) s/Str
   (s/required-key :location/street-number) s/Str
   (s/required-key :location/zip-code) s/Str
   (s/required-key :location/city) s/Str})  

(def RealEstate
  {(s/required-key :real-estate/title) s/Str
   (s/required-key :real-estate/description) s/Str
   (s/required-key :real-estate/type) res-types
   (s/required-key :real-estate/cost) s/Str
   (s/required-key :real-estate/size) s/Str
   (s/required-key :real-estate/bedrooms) s/Str
   (s/required-key :real-estate/features) features
   (s/required-key :real-estate/location) Location})

(def Ad 
  {(s/optional-key :ad/public-id) s/Str
   (s/required-key :ad/type) (s/eq :ad.type/real-estate)
   (s/required-key :ad/start-time) s/Str
   (s/required-key :ad/end-time) s/Str
   (s/required-key :ad/active) boolean
   (s/required-key :ad/real-estate) RealEstate})

(defn- coerce [ad]
  (update-in 
    ad [:ad/real-estate :real-estate/features] 
    (fn [coll] 
      (if (set? coll) 
        (vec coll) 
        coll))))

(defn validate-ad [ad]
  (s/validate Ad (coerce ad)))

(defn validate-ad-id [ad-id]
  (s/validate s/Str ad-id))
