(ns klusterz.kubernetes
  (:import (org.apache.ignite.spi.discovery.tcp.ipfinder TcpDiscoveryIpFinderAdapter)
           (io.fabric8.kubernetes.client KubernetesClient DefaultKubernetesClient)
           (io.fabric8.kubernetes.api.model Pod)
           (java.net InetSocketAddress)))


(defn get-hostname []
  (System/getenv "HOSTNAME"))

(defn ^Integer get-ignite-port []
  (Integer/parseInt (or (System/getenv "IGNITE_PORT") "5000")))

(defn ^DefaultKubernetesClient get-client []
  (let [master-host (System/getenv "KUBERNETES_SERVICE_HOST")
        master-port (System/getenv "KUBERNETES_SERVICE_PORT_HTTPS")]
    (if (and master-host master-port)
      (DefaultKubernetesClient. (str "https://" master-host ":" master-port))
      (DefaultKubernetesClient.))))

(defn get-labels [^Pod pod]
  (some->> (.getMetadata pod) (.getLabels)))

(defn get-pod ^Pod [^KubernetesClient client ^String pod-name]
  (.get (.withName (.pods client) pod-name)))

(defn get-pods [^KubernetesClient client labels]
  (.list (.withLabels (.pods client) labels)))

(defn pod-ip [^Pod pod]
  (let [ip   (.getPodIP (.getStatus pod))
        port (get-ignite-port)]
    (InetSocketAddress. ip port)))

(defn get-running-pods [^KubernetesClient client labels]
  (->> (get-pods client labels) (mapv pod-ip)))

(defn get-pod-ip-finder []
  (let [client    (get-client)
        me        (get-hostname)
        pod       (get-pod client me)
        addresses (atom [(pod-ip pod)])
        selector  (select-keys (get-labels pod) ["pod-template-hash"])
        discovery (proxy [TcpDiscoveryIpFinderAdapter] []
                    (registerAddresses [this addresses])
                    (unregisterAddresses [this addresses])
                    (getRegisteredAddresses [this]
                      (try
                        (swap! addresses (get-running-pods client selector))
                        (catch Exception e (deref addresses))))
                    (close [] (.close)))]
    (.setShared discovery true)
    discovery))

