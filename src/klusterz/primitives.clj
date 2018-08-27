(ns klusterz.primitives
  (:import (clojure.lang IRef IAtom2)
           (org.apache.ignite Ignition)
           (java.io Closeable))
  (:require [æsahættr :as æs]))


(defn ignite-atom [key state]
  (let [ignite (Ignition/ignite)
        inner  (atom state)
        ident  (str (æs/hash-object (æs/md5) æs/nippy-funnel key))
        ref    (.atomicReference ignite ident inner true)]
    (proxy [IAtom2 IRef Closeable] []
      (swap [this f]
        (second (.swapVals this f)))
      (swap [this f x]
        (.swap this (fn [v] (f v x))))
      (swap [this f x y]
        (.swap this (fn [v] (f v x y))))
      (swap [this f x y &args]
        (.swap this (fn [v] (apply f v x y &args))))
      (compareAndSet [this current value]
        (.compareAndSet ref current value))
      (reset [this value]
        (.set ref value))
      (swapVals [this f]
        (loop [old-value (.deref this)]
          (let [new-value (f old-value)]
            (if-not (.compareAndSet this old-value new-value)
              (recur (.deref this))
              [old-value new-value]))))
      (swapVals [this f x]
        (.swapVals this (fn [v] (f v x))))
      (swapVals [this f x y]
        (.swapVals this (fn [v] (f v x y))))
      (swapVals [this f x y &args]
        (.swapVals this (fn [v] (apply f v x y &args))))
      (resetVals [this value]
        (.swapVals this (constantly value)))
      (deref [this]
        (.get ref))
      (close [this]
        (.close ref)))))