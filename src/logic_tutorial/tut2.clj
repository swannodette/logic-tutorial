(ns logic-tutorial.tut2
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [appendo]]))

(defn appendo [l1 l2 o]
  (conde
    ((== l1 ()) (== l2 o))
    ((fresh [a d r]
       (conso a d l1)
       (conso a r o)
       (appendo d l2 r)))))
