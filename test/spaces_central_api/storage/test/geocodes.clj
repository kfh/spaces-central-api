(ns spaces-central-api.storage.test.geocodes
  (:require [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [spaces-central-api.storage.ads :as storage] 
            [spaces-central-api.system :refer [spaces-test-db]]
            [spaces-central-api.storage.geocodes :as geocodes]))

(deftest create-ad-and-find-geocode
  (let [system (component/start (spaces-test-db))
        {:keys [db]} system
        {:keys [conn]} db]
    (try 
      (testing "Creating ad and finding geocode"
        (let [geocode {:geocode/latitude 13.734603
                       :geocode/longitude 100.5639662}
              location {:location/name "Sukhumvit Road"
                        :location/street "Sukhumvit Road"
                        :location/street-number "413"
                        :location/zip-code "10110"
                        :location/city "Bangkok"
                        :location/geocode geocode}
              real-estate {:real-estate/title "New apartment in central Sukhumvit"                         
                           :real-estate/description "Beatiful apartment with perfect location.."
                           :real-estate/type :real-estate.type/apartment
                           :real-estate/cost "100 000 "
                           :real-estate/size "95 m2"
                           :real-estate/bedrooms "3"
                           :real-estate/features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                           :real-estate/location location}
              new-ad (storage/create-ad 
                       conn 
                       {:ad/type :ad.type/real-estate 
                        :ad/start-time "14:45" 
                        :ad/end-time "20:00" 
                        :ad/active true
                        :ad/real-estate real-estate})]
          (let [stored-ad (storage/get-ad conn (:ad/public-id new-ad))]
            (is (= (:ad/public-id new-ad) (:ad/public-id stored-ad)))
            (let [stored-geo (-> stored-ad :ad/real-estate :real-estate/location :location/geocode)
                  found-geo (geocodes/find-geocode conn (dissoc location :location/geocode))]
              (is (= (:geocode/latitude stored-geo) (:geocode/latitude found-geo)))
              (is (= (:geocode/longitude stored-geo) (:geocode/longitude found-geo)))))))
      (finally
        (component/stop system)))))
