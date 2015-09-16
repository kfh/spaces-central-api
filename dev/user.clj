(ns user
  (:require [spaces-central-api.system :refer [spaces-system]] 
            [reloaded.repl :refer [system init start stop go reset]]))

(reloaded.repl/set-init! 
  #(spaces-system {:db-name "spaces" 
                   :db-schema "resources/spaces-central-api-schema.edn"
                   :web-host "127.0.0.1" 
                   :web-port 3333}))
