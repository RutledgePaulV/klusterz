# klusterz

A Clojure library that provides distributed versions of Clojure's reference types via 
Apache Ignite and provides auto discovery among pods in the same deployment for kubernetes. 
The goal is to make applications with straightforward clustering needs easy to create.

This library requires no additional infrastructure, just an exposed port on each pod
that it can use for communications.

### Usage

```clojure 
; somewhere when you start up your app
(require '[klusterz.core :as k])
(k/start-server {:port 5000})

; then use the distributed primitives and you'll see
; immediately consistent values across all pods.
(require '[klusterz.primitives :refer :all])
(def distributed-sessions (ignite-atom :SESSIONS {}))
(get @distributed-sessions session-id)

```


### Caveats

This project is in its infancy and I don't have a lot of experience with Apache Ignite. You might
be interested in [Ashtree](https://github.com/vermilionsands/ashtree/) as a more developed wrapper but it looks
to me like they're more interested in the distributed computing and I'm more interested in recreating something
like [Avout](https://github.com/liebke/avout) but without the need for zookeeper.