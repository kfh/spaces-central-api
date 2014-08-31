(ns spaces-central-api.storage.test.ads
  (:require [datomic.api :as d]
            [hara.common :refer [uuid]] 
            [taoensso.timbre :as timbre]
            [spaces-central-api.storage.ads :as ads]  
            [spaces-central-api.system :refer [spaces-test-db]]  
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]))

(timbre/refer-timbre)

(deftest create-and-get-ad
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving an ad"
        (let [new-ad (ads/create-ad 
                       conn 
                       {:type :ad.type/real-estate 
                        :start-time "14:45" 
                        :end-time "20:00" 
                        :active true})]
          (let [stored-ad (ads/get-ad conn (:id new-ad))]
            (is (= (:type new-ad) (:type stored-ad)))
            (is (= (:start-time new-ad) (:start-time stored-ad)))
            (is (= (:end-time new-ad) (:end-time stored-ad)))
            (is (= (:active new-ad) (:active stored-ad))))))
      (finally
        (component/stop system)))))

(deftest create-and-get-all-ads
  (let [system (component/start (spaces-test-db))
        {:keys [datomic]} system
        {:keys [conn]} datomic]
    (try 
      (testing "Creating and retreiving all ads"
        (ads/create-ad conn {:type :ad.type/real-estate 
                             :start-time "14:45" 
                             :end-time "20:00" 
                             :active true})
        (ads/create-ad conn {:type :ad.type/real-estate 
                             :start-time "09:00" 
                             :end-time "21:00" 
                             :active true}) 
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
                       {:type :ad.type/real-estate 
                        :start-time "14:45" 
                        :end-time "20:00" 
                        :active true})]
          (let [updated-ad (assoc new-ad :start-time "12:00" :end-time "23:00")
                _ (ads/update-ad conn updated-ad)
                stored-ad (ads/get-ad conn (:id updated-ad))]
            (is (= (:start-time updated-ad) (:start-time stored-ad)))
            (is (= (:end-time updated-ad) (:end-time stored-ad))))))
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
                       {:type :ad.type/real-estate 
                        :start-time "00:00" 
                        :end-time "07:00" 
                        :active true})]
          (is (= 1 (count (ads/delete-ad conn (:id new-ad)))))))
      (finally 
        (component/stop system)))))
