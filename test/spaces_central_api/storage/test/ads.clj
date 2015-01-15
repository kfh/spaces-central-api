(ns spaces-central-api.storage.test.ads
  (:require [spaces-central-api.storage.ads :as ads]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [spaces-central-api.system :refer [spaces-test-db]]))

(deftest create-and-get-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving an ad"
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
              new-ad (ads/create-ad 
                       conn 
                       {:ad/type :ad.type/real-estate 
                        :ad/start-time "14:45" 
                        :ad/end-time "20:00" 
                        :ad/active true
                        :ad/real-estate real-estate})]

          (let [stored-ad (ads/get-ad conn (:ad/public-id new-ad))]
            (is (= (:ad/public-id new-ad) (:ad/public-id stored-ad)))   
            (is (= (:ad/type new-ad) (:ad/type stored-ad)))
            (is (= (:ad/start-time new-ad) (:ad/start-time stored-ad)))
            (is (= (:ad/end-time new-ad) (:ad/end-time stored-ad)))
            (is (= (:ad/active new-ad) (:ad/active stored-ad)))
            (let [new-res (:ad/real-estate new-ad)
                  stored-res (:ad/real-estate stored-ad)]
              (is (= (:real-estate/title new-res) (:real-estate/title stored-res)))   
              (is (= (:real-estate/description new-res) (:real-estate/description stored-res)))   
              (is (= (:real-estate/type new-res) (:real-estate/type stored-res)))   
              (is (= (:real-estate/cost new-res) (:real-estate/cost stored-res)))   
              (is (= (:real-estate/size new-res) (:real-estate/size stored-res)))   
              (is (= (:real-estate/bedrooms new-res) (:real-estate/bedrooms stored-res)))   
              (is (= (:real-estate/features new-res) (:real-estate/features stored-res)))   
              (let [new-loc (:real-estate/location new-res)
                    stored-loc (:real-estate/location stored-res)]
                (is (= (:location/name new-loc) (:location/name stored-loc)))   
                (is (= (:location/street new-loc) (:location/street stored-loc)))   
                (is (= (:location/street-number new-loc) (:location/street-number stored-loc)))      
                (is (= (:location/zip-code new-loc) (:location/zip-code stored-loc)))      
                (is (= (:location/city new-loc) (:location/city stored-loc)))
                (let [new-geo (:location/geocode new-loc)
                      stored-geo (:location/geocode stored-loc)]
                  (is (= (:geocode/latitude new-geo) (:geocode/latitude stored-geo)))   
                  (is (= (:geocode/longitude new-geo) (:geocode/longitude stored-geo)))))))))      
      (finally
        (component/stop system)))))

(deftest create-and-get-all-ads
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving all ads"
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
                           :real-estate/location location}]
          (ads/create-ad 
            conn 
            {:ad/type :ad.type/real-estate 
             :ad/start-time "14:45"  
             :ad/end-time "20:00"  
             :ad/active true
             :ad/real-estate real-estate}))
        (let [geocode {:geocode/latitude 13.7315902 
                       :geocode/longitude 100.56822}
              location {:location/name "Silom Road" 
                        :location/street "Silom Road" 
                        :location/street-number "1055" 
                        :location/zip-code "10110" 
                        :location/city "Bangkok"  
                        :location/geocode geocode}
              real-estate {:real-estate/title "Small and cosy apartment close to Lebua" 
                           :real-estate/description "Sourrounded by the huge Lebua tower u find.." 
                           :real-estate/type :real-estate.type/apartment
                           :real-estate/cost "245 000" 
                           :real-estate/size "72 m2" 
                           :real-estate/bedrooms "2" 
                           :real-estate/features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                           :real-estate/location location}]
          (ads/create-ad 
            conn 
            {:ad/type :ad.type/real-estate 
             :ad/start-time "09:00"  
             :ad/end-time "19:00"  
             :ad/active true
             :ad/real-estate real-estate}))
        (is (= 2 (count (ads/get-ads conn)))))
      (finally
        (component/stop system)))))

(deftest create-and-update-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and updating an ad"
        (let [geocode {:geocode/latitude 13.7315902 
                       :geocode/longitude 100.56822}
              location {:location/name "Silom Road" 
                        :location/street "Silom Road" 
                        :location/street-number "1055" 
                        :location/zip-code "10110" 
                        :location/city "Bangkok"  
                        :location/geocode geocode}
              real-estate {:real-estate/title "Small and cosy apartment close to Lebua" 
                           :real-estate/description "Sourrounded by the huge Lebua tower u find.." 
                           :real-estate/type :real-estate.type/apartment
                           :real-estate/cost "245 000" 
                           :real-estate/size "72 m2" 
                           :real-estate/bedrooms "2" 
                           :real-estate/features [:real-estate.feature/elevator :real-estate.feature/aircondition]
                           :real-estate/location location}
              new-ad (ads/create-ad 
                       conn 
                       {:ad/type :ad.type/real-estate 
                        :ad/start-time "09:00"  
                        :ad/end-time "19:00"  
                        :ad/active true
                        :ad/real-estate real-estate})]
          (let [updated-ad (-> (assoc new-ad :ad/start-time "12:00") 
                               (assoc-in [:ad/real-estate :real-estate/title] "Not so cosy apartment")
                               (assoc-in [:ad/real-estate :real-estate/location :location/name] "Not Silom Road"))             
                _ (ads/update-ad conn updated-ad)
                stored-ad (ads/get-ad conn (:ad/public-id updated-ad))]
            (is (= (:ad/start-time updated-ad) (:ad/start-time stored-ad)))
            (let [updated-res (:ad/real-estate updated-ad)
                  stored-res (:ad/real-estate stored-ad)]
              (is (= (:real-estate/title updated-res) (:real-estate/title stored-res)))
              (let [updated-loc (:real-estate/location updated-res)
                    stored-loc (:real-estate/location stored-res)]
                (is (= (:location/name updated-loc) (:location/name stored-loc))))))))
      (finally
        (component/stop system)))))

(deftest create-and-delete-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try
      (testing "Creating and deleting an ad"
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
              new-ad (ads/create-ad 
                       conn 
                       {:ad/type :ad.type/real-estate 
                        :ad/start-time "14:45"  
                        :ad/end-time "20:00"  
                        :ad/active true
                        :ad/real-estate real-estate})]
          (is (= 4 (count (ads/delete-ad conn (:ad/public-id new-ad)))))
          (is (nil? (ads/get-ad conn (:ad/public-id new-ad))))))
      (finally 
        (component/stop system)))))
