(ns spaces-central-api.gateway.test.geocoder
    (:require [clojure.test :refer [deftest testing is]]
              [com.stuartsierra.component :as component]
              [spaces-central-api.system :refer [spaces-test-geocoder]]
              [spaces-central-api.gateway.geocoder :refer [geocode-address geocode-location]]))

(deftest geocode-an-address
  (let [system (component/start (spaces-test-geocoder))
        {:keys [geocoder]} system]
    (try
      (testing "Geocoding an address"
               (let [address {:loc-street "Sukhumvit Road" :loc-street-num "413" :loc-zip-code "10110" :loc-city "Bangkok"}  
                     result (-> (geocode-address (:type geocoder) address) first)
                     {:keys [lng lat]} (-> result :geometry :location)]
                 (is (= (:formatted-address result) "413 Sukhumvit Road, Khlong Toei, Khlong Toei, Bangkok 10110, Thailand"))  
                 (is (= lng 100.5646072))
                 (is (= lat 13.7341553))))  
      (finally
        (component/stop system)))))

(deftest geocode-a-location
  (let [system (component/start (spaces-test-geocoder))
        {:keys [geocoder]} system]
    (try
      (testing "Geocoding a location"
               (let [location {:long 100.5646072 :lat 13.7341553}  
                     result (-> (geocode-location (:type geocoder) location) first)
                     {:keys [lng lat]} (-> result :geometry :location)]
                 (is (= lng 100.5646072))
                 (is (= lat 13.7341553))
                 (is (= (:formatted-address result) "14-493 Sukhumvit Road, Khlong Toei, Khlong Toei, Bangkok 10110, Thailand"))))
      (finally
        (component/stop system)))))
