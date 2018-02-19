(ns klusterz-example.core
  (:require [klusterz.core :as klusterz]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.json :as json])
  (:gen-class))

(defn ring-port []
  (Integer/parseInt
    (System/getenv "RING_PORT")))

(defn klusterz-port []
  (Integer/parseInt
    (System/getenv "KLUSTERZ_PORT")))

(defn klusterz-secret []
  (System/getenv "KLUSTERZ_SECRET"))

(defn kubernetes-api []
  (System/getenv "KUBERNETES_API"))

(defn klusterz-settings []
  {:kubernetes-api  (kubernetes-api)
   :klusterz-port   (klusterz-port)
   :klusterz-secret (klusterz-secret)})

(defn ring-settings []
  {:port (ring-port)})

(defn handler [request]
  {:body []})

(alter-var-root #'handler json/wrap-json-response)

(alter-var-root #'handler #(defaults/wrap-defaults % defaults/api-defaults))

(defn -main [& args]
  (klusterz/cluster! (klusterz-settings))
  (jetty/run-jetty handler (ring-settings)))

