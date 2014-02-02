(ns forking-chess.piece
  (:require [clojure.set :refer [map-invert]]))

(def column-to-int
  (zipmap (seq "abcdefgh")
          (range)))

(def int-to-column
  (map-invert column-to-int))

(defn position-to-coords [position]
  {:x (-> position first column-to-int)
   :y (-> position last str js/parseInt dec)})
(defn coords-to-position [{:keys [x y]}]
  (str (int-to-column x)
       (inc y)))

(defn available-moves [piece]
  (let [position (:position piece)
        {:keys [x y] :as coords} (position-to-coords position)]
    (when (= :P (get-in piece [:value :type]))
      (if (= "white" (get-in piece [:value :color]))
        (map coords-to-position [{:y (inc y) :x x}])
        (map coords-to-position [{:y (dec y) :x x}])))))


;;;;;;;;;;;;;
(comment
  (def white {:position "h2" :value {:color "white" :type :P}})
  (def black {:position "a7" :value {:color "black" :type :P}})
  (available-moves white)
  (available-moves black))
