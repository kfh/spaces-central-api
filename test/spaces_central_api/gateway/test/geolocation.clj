(ns spaces-central-api.gateway.test.geolocation
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [spaces-central-api.system :refer [spaces-test-system]]
            [spaces-central-api.gateway.geolocation :refer [search-by-address search-by-coordinates]]))

(deftest lookup-address
  (let [{:keys [geolocation] :as system} (component/start (spaces-test-system))]
    (try
      (testing "Retreiving coordinates for Buerskogen 11, Sandefjord"
        (let [result (first (search-by-address geolocation "Buerskogen 11, Sandefjord"))
              {:keys [latitude longitude]} (:coordinates result)]
          (is (= (:address result) "Buerskogen 11, 3234 Sandefjord, Norway"))
          (is (= latitude 59.09418650000001))
          (is (= longitude 10.2655836))))
      (finally
        (component/stop system)))))

(deftest lookup-coordinates
  (let [{:keys [geolocation] :as system} (component/start (spaces-test-system))]
    (try
      (testing "Retreiving address for coordinates belonging to Buerskogen 11, Sandefjord"
        (let [result (first (search-by-coordinates geolocation 59.09418650000001 10.2655836))
              {:keys [latitude longitude]} (:coordinates result)]
          (is (= (:address result) "Buerskogen 11, 3234 Sandefjord, Norway"))
          (is (= latitude 59.09418650000001))
          (is (= longitude 10.2655836))))
      (finally
        (component/stop system)))))
