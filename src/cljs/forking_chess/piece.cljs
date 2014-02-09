(ns forking-chess.piece
  (:require [clojure.set :refer [map-invert]]))

(def board-size 8)

(def column-to-int
  (zipmap (seq "abcdefgh")
          (range)))

(def int-to-column
  (map-invert column-to-int))


(defn position-to-coords [position]
  {:x (-> position first column-to-int)
   :y (-> position last str js/parseInt dec)})

(defn coords-to-position [x y]
  (str (int-to-column x)
       (inc y)))

(defn piece-to-coords [piece]
  (-> piece :position position-to-coords))

(defmulti available-moves #(get-in % [:value :type]))

(defmethod available-moves :P [pawn]
  (let [{:keys [x y]} (piece-to-coords pawn)]
    (if (= "white" (get-in pawn [:value :color]))
      (vector (coords-to-position x (inc y)))
      (vector (coords-to-position x (dec y))))))

;;;;;;;;;;;;;
(comment
  (def white {:position "h2" :value {:color "white" :type :P}})
  (def black {:position "a7" :value {:color "black" :type :P}})
  (available-moves white)
  (available-moves black))
