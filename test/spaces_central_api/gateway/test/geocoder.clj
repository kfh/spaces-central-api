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
        (let [address {:street "Sukhumvit Road" :street-number "413" :zip "10110" :city "Bangkok"}  
              result (-> (geocode-address (:type geocoder) address) first)
              {:keys [lng lat]} (-> result :geometry :location)]
          (is (= (:formatted-address result) "413 Sukhumvit Road, Khlong Toei, Khlong Toei, Bangkok 10110, Thailand"))  
          (is (= lng 100.5639662))
          (is (= lat 13.734603))))  
      (finally
        (component/stop system)))))

(deftest geocode-a-location
  (let [system (component/start (spaces-test-geocoder))
        {:keys [geocoder]} system]
    (try
      (testing "Geocoding a location"
        (let [location {:long 100.5639662 :lat 13.734603}  
              result (-> (geocode-location (:type geocoder) location) first)
              {:keys [lng lat]} (-> result :geometry :location)]
          (is (= lng 100.5639662))
          (is (= lat 13.734603))
          (is (= (:formatted-address result) "11-487 Sukhumvit Road, Khlong Toei, Khlong Toei, Bangkok 10110, Thailand"))))
      (finally
        (component/stop system)))))
