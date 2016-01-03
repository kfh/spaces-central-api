(ns spaces-central-api.domain.ads
  (:require [schema.core :as s])
  (:import (java.util UUID)))

(def Types (s/enum :real-estate.type/apartment :real-estate.type/house))

(def Features (s/enum :real-estate.feature/elevator :real-estate.feature/aircondition
                              :real-estate.feature/fireplace :real-estate.feature/lawn
                              :real-estate.feature/garage))

(s/defschema Location
  {:location/name          s/Str
   :location/street        s/Str
   :location/street-number s/Str
   :location/zip-code      s/Str
   :location/city          s/Str})

(s/defschema RealEstate
  {:real-estate/title       s/Str
   :real-estate/description s/Str
   :real-estate/type        Types
   :real-estate/cost        s/Str
   :real-estate/size        s/Str
   :real-estate/bedrooms    s/Str
   :real-estate/features    #{Features}
   :real-estate/location    Location})

(s/defschema Ad
  {:ad/public-id   UUID
   :ad/type        (s/eq :ad.type/real-estate)
   :ad/start-time  s/Str
   :ad/end-time    s/Str
   :ad/active      Boolean
   :ad/real-estate RealEstate})

(s/defschema NewAd (dissoc Ad :ad/public-id))

