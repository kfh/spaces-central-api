(ns spaces-central-api.storage.test.ads
  (:require [spaces-central-api.storage.ads :as ads]  
            [spaces-central-api.system :refer [spaces-test-db]]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]))

(deftest create-and-get-ad
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
            (is (= (:ad-type new-ad) (:ad-type stored-ad)))
            (is (= (:ad-start-time new-ad) (:ad-start-time stored-ad)))
            (is (= (:ad-end-time new-ad) (:ad-end-time stored-ad)))
            (is (= (:ad-active new-ad) (:ad-active stored-ad)))
            (is (= (:res-title new-ad) (:res-title stored-ad)))   
            (is (= (:res-desc new-ad) (:res-desc stored-ad)))   
            (is (= (:res-type new-ad) (:res-type stored-ad)))   
            (is (= (:res-cost new-ad) (:res-cost stored-ad)))   
            (is (= (:res-size new-ad) (:res-size stored-ad)))   
            (is (= (:res-bedrooms new-ad) (:res-bedrooms stored-ad)))   
            (is (= (:res-features new-ad) (:res-features stored-ad)))   
            (is (= (:loc-name new-ad) (:loc-name stored-ad)))   
            (is (= (:loc-street new-ad) (:loc-street stored-ad)))   
            (is (= (:loc-street-num new-ad) (:loc-street-num stored-ad)))      
            (is (= (:loc-zip-code new-ad) (:loc-zip-code stored-ad)))      
            (is (= (:loc-city new-ad) (:loc-city stored-ad))))))      
      (finally
        (component/stop system)))))

(deftest create-and-get-all-ads
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving all ads"
        (ads/create-ad conn {:ad-type :ad.type/real-estate 
                             :ad-start-time "14:45" 
                             :ad-end-time "20:00" 
                             :ad-active true
                             :res-title "New apartment in central Sukhumvit"
                             :res-desc "Beatiful apartment with perfect location.."
                             :res-type :real-estate.type/apartment
                             :res-cost "100 000"
                             :res-size "95 m2"
                             :res-bedrooms "3"
                             :res-features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                             :loc-name "Sukhumvit Road"
                             :loc-street "Sukhumvit Road"
                             :loc-street-num "413"
                             :loc-zip-code "10110"
                             :loc-city "Bangkok" 
                             :geo-lat 13.734603
                             :geo-long 100.5639662})
        (ads/create-ad conn {:ad-type :ad.type/real-estate 
                             :ad-start-time "09:00" 
                             :ad-end-time "19:00" 
                             :ad-active true
                             :res-title "Small and cosy apartment close to Lebua"
                             :res-desc "Sourrounded by the huge Lebua tower u find.."
                             :res-type :real-estate.type/apartment
                             :res-cost "245 000"
                             :res-size "72 m2"
                             :res-bedrooms "2"
                             :res-features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                             :loc-name "Silom Road"
                             :loc-street "Silom Road"
                             :loc-street-num "1055"
                             :loc-zip-code "10110"
                             :loc-city "Bangkok" 
                             :geo-lat 13.7315902
                             :geo-long 100.56822}) 
        (is (= 2 (count (ads/get-ads conn)))))
      (finally
        (component/stop system)))))

(deftest create-and-update-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and updating an ad"
        (let [new-ad (ads/create-ad 
                       conn 
                       {:ad-type :ad.type/real-estate 
                        :ad-start-time "09:00" 
                        :ad-end-time "19:00" 
                        :ad-active true
                        :res-title "Small and cosy apartment close to Lebua"
                        :res-desc "Sourrounded by the huge Lebua tower u find.."
                        :res-type :real-estate.type/apartment
                        :res-cost "245 000"
                        :res-size "72 m2"
                        :res-bedrooms "2"
                        :res-features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                        :loc-name "Silom Road"
                        :loc-street "Silom Road"
                        :loc-street-num "1055"
                        :loc-zip-code "10110"
                        :loc-city "Bangkok" 
                        :geo-lat 13.7315902
                        :geo-long 100.56822})]
          (let [updated-ad (assoc new-ad :ad-start-time "12:00" :res-title "Not so cosy apartment" :loc-name "Not Silom Road")
                _ (ads/update-ad conn updated-ad)
                stored-ad (ads/get-ad conn (:ad-id updated-ad))]
            (is (= (:ad-start-time updated-ad) (:ad-start-time stored-ad)))
            (is (= (:res-title updated-ad) (:res-title stored-ad)))
            (is (= (:loc-name updated-ad) (:loc-name stored-ad))))))
      (finally
        (component/stop system)))))

(deftest create-and-delete-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try
      (testing "Creating and deleting an ad"
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
          (is (= 4 (count (ads/delete-ad conn (:ad-id new-ad)))))))
      (finally 
        (component/stop system)))))
