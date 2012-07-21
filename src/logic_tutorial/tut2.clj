(ns logic-tutorial.tut2
  (:refer-clojure :exlude [reify == inc])
  (:use [clojure.core.logic minikanren prelude]))

(defn appendo [l1 l2 o]
  (conde
    ((== l1 ()) (== l2 o))
    ((exist [a d r]
       (conso a d l1)
       (conso a r o)
       (appendo d l2 r)))))
