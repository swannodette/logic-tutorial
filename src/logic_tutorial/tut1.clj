(ns logic-tutorial.tut1
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defrel parent x y)
(defrel male x)
(defrel female x)

(defn child [x y]
  (parent y x))

(defn son [x y]
  (all
   (child x y)
   (male x)))

(defn daughter [x y]
  (all
   (child x y)
   (female x)))

(defn grandparent [x y]
  (fresh [z]
    (parent x z)
    (parent z y)))

(defn granddaughter [x y]
  (fresh [z]
    (daughter x z)
    (child z y)))
