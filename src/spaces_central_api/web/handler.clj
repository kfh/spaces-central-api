(ns spaces-central-api.web.handler
  (:require [schema.core :as s]
            [taoensso.timbre :as timbre]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [com.stuartsierra.component :as component]
            [spaces-central-api.domain.ads :refer :all]
            [ring.swagger.schema :refer [coerce!]]
            [spaces-central-api.service.ads :as service])
  (:import (java.util UUID)))

(timbre/refer-timbre)

(defrecord RingHandler [db geocoder]
  component/Lifecycle

  (start [this]
    (info "Enabling ring handler")
    (if (:handler this)
      this
      (->> (api
             (ring.swagger.ui/swagger-ui
               "/swagger-ui")
             (swagger-docs
               {:info {:title "Spaces Central API"}})
             {:formats [:transit-json :edn]}
             ;{:params-opts {:transit-json {:options {:handlers :readers}}}}
             {:response-opts {:transit-json {:options {:handlers :writers}}}}
             (context* "/api" []
                       :tags ["ads"]

                       (GET* "/ads" []
                             :return [Ad]
                             :summary "returns a list of all ads."
                             (ok (service/get-ads (:conn db))))

                       (GET* "/ads/:id" []
                             :return Ad
                             :path-params [id :- UUID]
                             :summary "returns the ad with given id."
                             (if-let [ad (service/get-ad (:conn db) id)] (ok ad) (not-found)))

                       (POST* "/ads" []
                              :return Ad
                              :body [ad NewAd]
                              :summary "creates a new ad."
                              (ok (service/create-ad (:conn db) (:type geocoder) ad)))

                       (PUT* "/ads" []
                             :return Ad
                             :body [ad Ad]
                             :summary "updates an existing ad."
                             (ok (service/update-ad (:conn db) (:type geocoder) ad)))

                       (DELETE* "/ads/:id" []
                                :path-params [id :- UUID]
                                :summary "deletes the ad with given id."
                                (if (service/delete-ad (:conn db) id) (no-content) (not-found)))))
             (assoc this :handler))))

  (stop [this]
    (info "Disabling ring handler")
    (if-not (:handler this)
      this
      (dissoc this :handler))))

(defn ring-handler []
  (component/using
    (map->RingHandler {})
    [:db :geocoder]))
