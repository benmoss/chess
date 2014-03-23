(ns forking-chess.utils
  (:require [clojure.set :refer [map-invert]]))

(def column-to-int
  (zipmap (seq "abcdefgh")
          (range 8)))

(defn position-to-coords [[column row]]
  [(column-to-int column)
   (-> row str js/parseInt dec)])

(defn strings-to-keywords [x]
  (into {} (for [[k v] x] [(keyword k) v])))
