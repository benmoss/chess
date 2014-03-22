(ns forking-chess.board
  (:require [clojure.string :as str]
            [forking-chess.piece :refer [position-to-coords]]))

(enable-console-print!)

(def base-column {"a" :R "b" :N "c" :B "d" :Q "e" :K "f" :B "g" :N "h" :R})

(def initial-placements
  (into {}
        (for [row [1 2 7 8]
              column (map str (seq "abcdefgh"))
              :let [color (if (< row 5) "white" "black")
                    type (if (or (= 1 row)
                                 (= 8 row))
                           (base-column column) :P)]]
          [(str column row) {:color color :type type}])))

(defn initial-placement [column row]
  (initial-placements (str column row)))

(def squares
  (into (sorted-map-by (fn [x y]
                         (let [[xrow xcol] (position-to-coords x)
                               [yrow ycol] (position-to-coords y)]
                           (compare [ycol xrow] [xcol yrow]))))
        (for [row (range 1 9)
              column (seq "abcdefgh")
              :let [position (str column row)
                    value (initial-placement column row)]]
          [position value])))

(def icons
  {#{"white" :K} \♔
   #{"white" :Q} \♕
   #{"white" :R} \♖
   #{"white" :B} \♗
   #{"white" :N} \♘
   #{"white" :P} \♙
   #{"black" :K} \♚
   #{"black" :Q} \♛
   #{"black" :R} \♜
   #{"black" :B} \♝
   #{"black" :N} \♞
   #{"black" :P} \♟})
