(defproject klusterz "0.1.0-SNAPSHOT"
  :description "A library for communication between instances deployed with kubernetes."
  :url "https://github.com/rutledgepaulv/klusterz"
  :license {:name "Unlicense" :url "http://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [cheshire "5.8.0"]
                 [io.fabric8/kubernetes-client "3.1.8"]])
