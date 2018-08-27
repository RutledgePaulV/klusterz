(ns klusterz.core
  (:require [klusterz.kubernetes :as k8s])
  (:import (org.apache.ignite Ignition)
           (org.apache.ignite.configuration IgniteConfiguration)
           (org.apache.ignite.spi.discovery.tcp TcpDiscoverySpi)))

(defn ^IgniteConfiguration get-default-config []
  (IgniteConfiguration.))

(defn get-kubernetes-config []
  (doto (get-default-config)
    (.setDiscoverySpi
      (doto (TcpDiscoverySpi.)
        (.setIpFinder (k8s/get-pod-ip-finder))))))

(defn start-server
  ([] (start-server (get-kubernetes-config)))
  ([^IgniteConfiguration config]
   (.setClientMode config false)
   (Ignition/start config)))

(defn start-client
  ([] (start-client (get-kubernetes-config)))
  ([^IgniteConfiguration config]
   (.setClientMode config true)
   (Ignition/start config)))