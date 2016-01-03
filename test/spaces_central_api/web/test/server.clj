(ns spaces-central-api.web.test.server
  (:require [clj-http.client :as http]
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [spaces-central-api.system :refer [spaces-test-system]]))

(deftest create-and-get-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try
      (testing "Creating and retreiving an ad"
        (let [location {:location/name          "Sukhumvit Road"
                        :location/street        "Sukhumvit Road"
                        :location/street-number "413"
                        :location/zip-code      "10110"
                        :location/city          "Bangkok"}
              real-estate {:real-estate/title       "New apartment in central Sukhumvit"
                           :real-estate/description "Beatiful apartment with perfect location.."
                           :real-estate/type        :real-estate.type/apartment
                           :real-estate/cost        "100 000 "
                           :real-estate/size        "95 m2"
                           :real-estate/bedrooms    "3"
                           :real-estate/features    #{:real-estate.feature/elevator :real-estate.feature/aircondition}
                           :real-estate/location    location}
              new-ad {:ad/type        :ad.type/real-estate
                      :ad/start-time  "14:45"
                      :ad/end-time    "20:00"
                      :ad/active      true
                      :ad/real-estate real-estate}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/ads"))
              res (post {:form-params new-ad :content-type :transit+json :as :transit+json})
              stored-ad (:body res)]
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
              (is (= (:location/city new-loc) (:location/city stored-loc)))))
          (let [get (partial http/get (str url "/api/ads/" (:ad/public-id stored-ad)))
                res (get {:accept :transit+json :as :transit+json})
                stored-ad (:body res)]
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
                (is (= (:location/city new-loc) (:location/city stored-loc))))))))
      (finally
        (component/stop system)))))

(deftest create-and-get-all-ads
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try
      (testing "Creating and retreiving all ads"
        (let [loc-1 {:location/name          "Sukhumvit Road"
                     :location/street        "Sukhumvit Road"
                     :location/street-number "413"
                     :location/zip-code      "10110"
                     :location/city          "Bangkok"}
              res-1 {:real-estate/title       "New apartment in central Sukhumvit"
                     :real-estate/description "Beatiful apartment with perfect location.."
                     :real-estate/type        :real-estate.type/apartment
                     :real-estate/cost        "100 000 "
                     :real-estate/size        "95 m2"
                     :real-estate/bedrooms    "3"
                     :real-estate/features    #{:real-estate.feature/elevator :real-estate.feature/aircondition}
                     :real-estate/location    loc-1}
              new-ad-1 {:ad/type        :ad.type/real-estate
                        :ad/start-time  "14:45"
                        :ad/end-time    "20:00"
                        :ad/active      true
                        :ad/real-estate res-1}
              loc-2 {:location/name          "Silom Road"
                     :location/street        "Silom Road"
                     :location/street-number "1055"
                     :location/zip-code      "10110"
                     :location/city          "Bangkok"}
              res-2 {:real-estate/title       "Small and cosy apartment close to Lebua"
                     :real-estate/description "Sourrounded by the huge Lebua tower u find.."
                     :real-estate/type        :real-estate.type/apartment
                     :real-estate/cost        "245 000"
                     :real-estate/size        "72 m2"
                     :real-estate/bedrooms    "2"
                     :real-estate/features    [:real-estate.feature/elevator :real-estate.feature/aircondition]
                     :real-estate/location    loc-2}
              new-ad-2 {:ad/type        :ad.type/real-estate
                        :ad/start-time  "09:00"
                        :ad/end-time    "19:00"
                        :ad/active      true
                        :ad/real-estate res-2}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/ads"))]
          (post {:form-params new-ad-1 :content-type :transit+json :as :transit+json})
          (post {:form-params new-ad-2 :content-type :transit+json :as :transit+json})
          (let [response (http/get (str url "/api/ads") {:as :transit+json})
                stored-ads (:body response)]
            (is (= 2 (count stored-ads))))))
      (finally
        (component/stop system)))))

(deftest create-and-update-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try
      (testing "Creating and updating an ad"
        (let [location {:location/name          "Silom Road"
                        :location/street        "Silom Road"
                        :location/street-number "1055"
                        :location/zip-code      "10110"
                        :location/city          "Bangkok"}
              real-estate {:real-estate/title       "Small and cosy apartment close to Lebua"
                           :real-estate/description "Sourrounded by the huge Lebua tower u find.."
                           :real-estate/type        :real-estate.type/apartment
                           :real-estate/cost        "245 000"
                           :real-estate/size        "72 m2"
                           :real-estate/bedrooms    "2"
                           :real-estate/features    #{:real-estate.feature/elevator :real-estate.feature/aircondition}
                           :real-estate/location    location}
              new-ad {:ad/type        :ad.type/real-estate
                      :ad/start-time  "09:00"
                      :ad/end-time    "19:00"
                      :ad/active      true
                      :ad/real-estate real-estate}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/ads"))
              create-res (post {:form-params new-ad :content-type :transit+json :as :transit+json})
              returned-ad (:body create-res)
              updated-ad (assoc returned-ad :ad/start-time "17:00" :ad/end-time "18:00")
              update (partial http/put (str url "/api/ads"))
              update-res (update {:form-params updated-ad :content-type :transit+json :as :transit+json})
              stored-ad (:body update-res)]
          (is (= (:ad/public-id updated-ad) (:ad/public-id stored-ad)))
          (is (= (:ad/type updated-ad) (:ad/type stored-ad)))
          (is (= (:ad/start-time updated-ad) (:ad/start-time stored-ad)))
          (is (= (:ad/end-time updated-ad) (:ad/end-time stored-ad)))
          (is (= (:ad/active updated-ad) (:ad/active stored-ad)))
          (let [updated-loc (-> updated-ad :ad/real-estate :real-estate/location)
                stored-loc (-> stored-ad :ad/real-estate :real-estate/location)]
            (is (= (:location/name updated-loc) (:location/name stored-loc))))))
      (finally
        (component/stop system)))))

(deftest create-and-delete-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try
      (testing "Creating and deleting an ad"
        (let [location {:location/name          "Sukhumvit Road"
                        :location/street        "Sukhumvit Road"
                        :location/street-number "413"
                        :location/zip-code      "10110"
                        :location/city          "Bangkok"}
              real-estate {:real-estate/title       "New apartment in central Sukhumvit"
                           :real-estate/description "Beatiful apartment with perfect location.."
                           :real-estate/type        :real-estate.type/apartment
                           :real-estate/cost        "100 000 "
                           :real-estate/size        "95 m2"
                           :real-estate/bedrooms    "3"
                           :real-estate/features    #{:real-estate.feature/elevator :real-estate.feature/aircondition}
                           :real-estate/location    location}
              new-ad {:ad/type        :ad.type/real-estate
                      :ad/start-time  "14:45"
                      :ad/end-time    "20:00"
                      :ad/active      true
                      :ad/real-estate real-estate}
              url (str "http://" (:host web-server) ":" (:port web-server))
              post (partial http/post (str url "/api/ads"))
              create-res (post {:form-params new-ad :content-type :transit+json :as :transit+json})
              stored-ad (:body create-res)]
          (is (= 204 (:status (http/delete (str url "/api/ads/" (:ad/public-id stored-ad))))))
          (is (= 404 (:status (http/get (str url "/api/ads/" (:ad/public-id stored-ad)) {:throw-exceptions false}))))))
      (finally
        (component/stop system)))))
