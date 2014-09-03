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
        (let [new-ad {:type "ad.type/real-estate" 
                      :start-time "14:45" 
                      :end-time "20:00" 
                      :active true}
              url (str "http://" (:host web-server) ":" (:port web-server))
              response (http/post (str url "/ads") {:form-params new-ad :content-type :json})
              returned-ad (-> response :body (json/parse-string true))]
          (is (= (:type new-ad) (:type returned-ad)))
          (is (= (:start-time new-ad) (:start-time returned-ad)))
          (is (= (:end-time new-ad) (:end-time returned-ad)))
          (is (= (:active new-ad) (:active returned-ad)))
          (let [response (http/get (str url "/ads/" (:id returned-ad)))
                stored-ad (-> response :body (json/parse-string true))]
            (is (= (:type new-ad) (:type stored-ad)))
            (is (= (:start-time new-ad) (:start-time stored-ad)))
            (is (= (:end-time new-ad) (:end-time stored-ad)))
            (is (= (:active new-ad) (:active stored-ad))))))
      (finally
        (component/stop system)))))

(deftest create-and-get-all-ads
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and retreiving all ads"
        (let [a-new-ad {:type "ad.type/real-estate" 
                      :start-time "14:45" 
                      :end-time "20:00" 
                      :active true}
              another-new-ad {:type "ad.type/real-estate" 
                      :start-time "06:45" 
                      :end-time "23:00" 
                      :active true}
              url (str "http://" (:host web-server) ":" (:port web-server))]
          (http/post (str url "/ads") {:form-params a-new-ad :content-type :json}) 
          (http/post (str url "/ads") {:form-params another-new-ad :content-type :json}) 
          (let [response (http/get (str url "/ads"))
                stored-ads (-> response :body (json/parse-string true))]
            (is (= 2 (count stored-ads))))))
      (finally
        (component/stop system)))))

(deftest create-and-update-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and updating an ad"
        (let [new-ad {:type "ad.type/real-estate" 
                      :start-time "14:45" 
                      :end-time "20:00" 
                      :active true}
              url (str "http://" (:host web-server) ":" (:port web-server))
              create-res (http/post (str url "/ads") {:form-params new-ad :content-type :json})
              returned-ad (-> create-res :body (json/parse-string true))
              updated-ad (assoc returned-ad :start-time "17:00" :end-time "18:00")
              update-res (http/put (str url "/ads/" (:id updated-ad)) {:form-params updated-ad :content-type :json})
              stored-ad (-> update-res :body (json/parse-string true))]
          (is (= (:id updated-ad) (:id stored-ad)))
          (is (= (:type updated-ad) (:type stored-ad)))
          (is (= (:start-time updated-ad) (:start-time stored-ad)))
          (is (= (:end-time updated-ad) (:end-time stored-ad)))
          (is (= (:active updated-ad) (:active stored-ad)))))
      (finally
        (component/stop system)))))

(deftest create-and-delete-ad
  (let [system (component/start (spaces-test-system))
        {:keys [web-server]} system]
    (try 
      (testing "Creating and deleting an ad"
        (let [new-ad {:type "ad.type/real-estate" 
                      :start-time "14:45" 
                      :end-time "20:00" 
                      :active true}
              url (str "http://" (:host web-server) ":" (:port web-server))
              create-res (http/post (str url "/ads") {:form-params new-ad :content-type :json})
              stored-ad (-> create-res :body (json/parse-string true))]
          (is (= 204 (:status (http/delete (str url "/ads/" (:id stored-ad))))))
          (is (= 404 (:status (http/get (str url "/ads/" (:id stored-ad)) {:throw-exceptions false}))))))  
      (finally
        (component/stop system)))))
