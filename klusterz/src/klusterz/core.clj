(ns klusterz.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as strings]
            [klusterz.client :as client]
            [clojure.core.async :as async]))

(def CLIENT (atom nil))

(defn run [& commands]
  (let [result (apply shell/sh commands)]
    (if-not (zero? (:exit result))
      (throw (Exception. ^String (:err result)))
      (:out result))))

(defn get-hostname []
  (strings/trim (run "hostname")))

(defn cluster! [{:keys [kubernetes-api]}]
  (let [client (reset! CLIENT (client/create-client kubernetes-api))]
    (client/with-client client
      (if (client/within-kubernetes?)
        (when-some [pod (client/get :pod (get-hostname))]
          (let [labels (get-in pod [:metadata :labels] {})
                events (client/watch :pod labels)]
            (async/go-loop [event (async/<! events)]
              (when (some? event)
                (println "Received event" event)
                (recur (async/<! events))))))
        (throw (IllegalStateException. "Not within k8s."))))))


