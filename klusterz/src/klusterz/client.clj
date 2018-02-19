(ns klusterz.client
  (:require [cheshire.core :as chesh]
            [clojure.string :as strings]
            [clojure.core.async :as async])
  (:import (com.fasterxml.jackson.databind ObjectMapper)
           (io.fabric8.kubernetes.client DefaultKubernetesClient Watcher Watch)
           (clojure.core.async.impl.channels ManyToManyChannel)
           (io.fabric8.kubernetes.api.model LabelSelector)
           (io.fabric8.kubernetes.client.dsl Filterable)))

(defn attach-channel-cleanup [^ManyToManyChannel chan f]
  (add-watch
    (.closed chan)
    "channel-resource-cleanup"
    (fn [_ _ old-state new-state]
      (when (and (not old-state) new-state)
        (f))))
  chan)

(defn obj->data [o]
  (chesh/parse-string
    (.writeValueAsString
      (ObjectMapper.)
      o)
    true))

(defn create-client
  ([] (DefaultKubernetesClient.))
  ([url] (DefaultKubernetesClient. ^String url)))

(def ^:dynamic *client*)

(defmacro with-client [client & body]
  `(binding [*client* ~client] ~@body))

(defn get-module [resource]
  (case (keyword resource)
    :namespace (.namespaces *client*)
    :ingress (.ingresses (.extensions *client*))
    :service (.services *client*)
    :deployment (.deployments (.extensions *client*))
    :pod (.pods *client*)
    :secret (.secrets *client*)))

(defn list [resource]
  (->> resource
       (get-module)
       (.list)
       (.getItems)
       (map obj->data)))

(defn get [resource name]
  (-> resource
      (get-module)
      (.withName name)
      (.get)
      (obj->data)))

(defn selector-from-labels [labels]
  (let [selector (LabelSelector.)
        strings  (into {} (map (fn [[k v]] [(name k) (name v)]) labels))]
    (.setMatchLabels selector strings)
    selector))

(defn watch
  ([resource] (watch resource {}))
  ([resource labels]
   (let [result  (async/chan)
         watcher (reify Watcher
                   (eventReceived [_ action resource]
                     (async/put! result
                                 [(keyword
                                    (strings/lower-case
                                      (.name action)))
                                  (obj->data resource)]))
                   (onClose [_ cause]
                     (async/close! result)))
         watch   (if (empty? labels)
                   (.watch (get-module resource) watcher)
                   (.watch (.withLabelSelector
                             ^Filterable (get-module resource)
                             (selector-from-labels labels)) watcher))]
     (attach-channel-cleanup result #(.close ^Watch watch)))))



(defn within-kubernetes? []
  (try (do (list :namespace) true)
       (catch Exception e
         (.printStackTrace e)
         false)))