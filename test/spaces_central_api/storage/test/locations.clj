(ns spaces-central-api.storage.test.locations
  (:require [spaces-central-api.storage.ads :as ads]
            [spaces-central-api.storage.locations :as locations]  
            [spaces-central-api.system :refer [spaces-test-db]]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]))

(deftest create-ad-and-find-location
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving an ad"
        (let [new-ad (ads/create-ad 
                       conn 
                       {:ad-type :ad.type/real-estate 
                        :ad-start-time "14:45" 
                        :ad-end-time "20:00" 
                        :ad-active true
                        :res-title "New apartment in central Sukhumvit"
                        :res-desc "Beatiful apartment with perfect location.."
                        :res-type :real-estate.type/apartment
                        :res-cost "100 000 "
                        :res-size "95 m2"
                        :res-bedrooms "3"
                        :res-features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                        :loc-name "Sukhumvit Road"
                        :loc-street "Sukhumvit Road"
                        :loc-street-num "413"
                        :loc-zip-code "10110"
                        :loc-city "Bangkok" 
                        :geo-lat 13.734603
                        :geo-long 100.5639662})]
          (let [stored-ad (ads/get-ad conn (:ad-id new-ad))]
            (is (= (:ad-id new-ad) (:ad-id stored-ad)))
            (let [location  (locations/find-location conn stored-ad)]
              (is (= (:loc-name stored-ad) (:loc-name location)))
              (is (= (:loc-street stored-ad) (:loc-street location)))
              (is (= (:loc-street-num stored-ad) (:loc-street-num location)))
              (is (= (:loc-zip-code stored-ad) (:loc-zip-code location)))
              (is (= (:loc-city stored-ad) (:loc-city location)))
              (is (= (:geo-lat stored-ad) (:geo-lat location)))
              (is (= (:geo-long stored-ad) (:geo-long location)))))))
      (finally
        (component/stop system)))))
