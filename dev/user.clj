(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [puget.printer :refer [cprint]]
            [spaces-central-api.system :as sys] 
            [com.stuartsierra.component :as component]
            [spaces-central-api.gateway.geolocation :as geolocation]))

(def system nil)

(defn init []
  (alter-var-root 
    #'system
    (constantly 
      (sys/spaces-system 
        {:db-name "spaces" 
         :db-schema "resources/spaces-central-api-schema.edn" 
         :http-port 4444
         :geolocation-url "https://maps.googleapis.com/maps/api/geocode/json?"}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
