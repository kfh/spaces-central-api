(ns spaces-central-api.web.test.server
  (:require [clj-http.client :as http] 
            [cheshire.core :as json] 
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]  
            [spaces-central-api.system :refer [spaces-test-system]]))

(deftest create-and-get-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and retreiving an ad"
        (let [new-ad {:ad-type "ad.type/real-estate"                       
                      :ad-start-time "14:45" 
                      :ad-end-time "20:00" 
                      :ad-active true
                      :res-title "New apartment in central Sukhumvit"
                      :res-desc "Beatiful apartment with perfect location.."
                      :res-type "real-estate.type/apartment"
                      :res-cost "100 000 "
                      :res-size "95 m2"
                      :res-bedrooms "3"
                      :res-features ["real-estate.feature/aircondition" "real-estate.feature/elevator"]
                      :loc-name "Sukhumvit Road"
                      :loc-street "Sukhumvit Road"
                      :loc-street-num "413"
                      :loc-zip-code "10110"
                      :loc-city "Bangkok"}
              url (str "http://" (:host web-server) ":" (:port web-server))
              response (http/post (str url "/api/ads") {:form-params new-ad :content-type :json})
              returned-ad (-> response :body (json/parse-string true))]
          (is (= (:ad-type new-ad) (:ad-type returned-ad)))
          (is (= (:ad-start-time new-ad) (:ad-start-time returned-ad)))
          (is (= (:ad-end-time new-ad) (:ad-end-time returned-ad)))
          (is (= (:ad-active new-ad) (:ad-active returned-ad)))
          (is (= (:res-title new-ad) (:res-title returned-ad)))   
          (is (= (:res-desc new-ad) (:res-desc returned-ad)))   
          (is (= (:res-type new-ad) (:res-type returned-ad)))   
          (is (= (:res-cost new-ad) (:res-cost returned-ad)))   
          (is (= (:res-size new-ad) (:res-size returned-ad)))   
          (is (= (:res-bedrooms new-ad) (:res-bedrooms returned-ad)))   
          (is (= (:res-features new-ad) (:res-features returned-ad)))   
          (is (= (:loc-name new-ad) (:loc-name returned-ad)))   
          (is (= (:loc-street new-ad) (:loc-street returned-ad)))   
          (is (= (:loc-street-num new-ad) (:loc-street-num returned-ad)))      
          (is (= (:loc-zip-code new-ad) (:loc-zip-code returned-ad)))      
          (is (= (:loc-city new-ad) (:loc-city returned-ad)))
          (let [response (http/get (str url "/api/ads/" (:ad-id returned-ad)))
                stored-ad (-> response :body (json/parse-string true))]
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
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and retreiving all ads"
        (let [a-new-ad {:ad-type :ad.type/real-estate 
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
                        :loc-city "Bangkok"}
              another-new-ad {:ad-type :ad.type/real-estate 
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
                              :loc-city "Bangkok"}
              url (str "http://" (:host web-server) ":" (:port web-server))]
          (http/post (str url "/api/ads") {:form-params a-new-ad :content-type :json}) 
          (http/post (str url "/api/ads") {:form-params another-new-ad :content-type :json}) 
          (let [response (http/get (str url "/api/ads"))
                stored-ads (-> response :body (json/parse-string true))]
            (is (= 2 (count stored-ads))))))
      (finally
        (component/stop system)))))

(deftest create-and-update-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and updating an ad"
        (let [new-ad {:ad-type :ad.type/real-estate 
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
                      :loc-city "Bangkok"}
              url (str "http://" (:host web-server) ":" (:port web-server))
              create-res (http/post (str url "/api/ads") {:form-params new-ad :content-type :json})
              returned-ad (-> create-res :body (json/parse-string true))
              updated-ad (assoc returned-ad :ad-start-time "17:00" :ad-end-time "18:00")
              update-res (http/put (str url "/api/ads/" (:ad-id updated-ad)) {:form-params updated-ad :content-type :json})
              stored-ad (-> update-res :body (json/parse-string true))]
          (is (= (:ad-id updated-ad) (:ad-id stored-ad)))
          (is (= (:ad-type updated-ad) (:ad-type stored-ad)))
          (is (= (:ad-start-time updated-ad) (:ad-start-time stored-ad)))
          (is (= (:ad-end-time updated-ad) (:ad-end-time stored-ad)))
          (is (= (:ad-active updated-ad) (:ad-active stored-ad)))
          (is (= (:loc-name updated-ad) (:loc-name stored-ad)))))
      (finally
        (component/stop system)))))

(deftest create-and-delete-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and deleting an ad"
        (let [new-ad {:ad-type :ad.type/real-estate 
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
                      :loc-city "Bangkok"}
              url (str "http://" (:host web-server) ":" (:port web-server))
              create-res (http/post (str url "/api/ads") {:form-params new-ad :content-type :json})
              stored-ad (-> create-res :body (json/parse-string true))]
          (is (= 204 (:status (http/delete (str url "/api/ads/" (:ad-id stored-ad))))))
          (is (= 404 (:status (http/get (str url "/api/ads/" (:ad-id stored-ad)) {:throw-exceptions false}))))))  
      (finally
        (component/stop system)))))
