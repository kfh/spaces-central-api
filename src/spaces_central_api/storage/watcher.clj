(ns spaces-central-api.storage.watcher
  (:require [datomic.api :as d]
            [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [>!! mult chan close! thread]]))

(timbre/refer-timbre)

(defrecord TxReportWatcher [datomic]
  component/Lifecycle

  (start [this]
    (info "Starting transaction report watcher")
    (if (:tx-publisher this) 
      this
      (let [tx-publisher (chan)
            tx-listener (mult tx-publisher)
            tx-queue (d/tx-report-queue (:conn datomic))]
        (thread 
          (while true
            (let [tx-report (.take tx-queue)]
              (>!! tx-publisher tx-report))))
        (assoc this :tx-publisher tx-publisher :tx-listener tx-listener))))

  (stop [this]
    (info "Stopping transaction report watcher")
    (if-not (:tx-publisher this) 
      this
      (do 
        (close! (:tx-publisher this))
        (dissoc this :tx-publisher :tx-listener)))))

(defn tx-report-watcher []
  (component/using 
    (map->TxReportWatcher {})
    [:datomic]))
