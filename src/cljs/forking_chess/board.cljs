(ns forking-chess.board
  (:require [clojure.string :as str]
            [forking-chess.piece :refer [position-to-coords]]))

(enable-console-print!)

(def base-row {"a" :R "b" :N "c" :B "d" :Q "e" :K "f" :B "g" :N "h" :R})

(def initial-placements
  (into {}
        (for [row (map str (seq "abcdefgh"))
              column [1 2 7 8]
              :let [color (if (< column 5) "white" "black")
                    type (if (or (= 1 column)
                                 (= 8 column))
                           (base-row row) :P)]]
          [(str row column) {:color color :type type}])))

(defn initial-placement [column row]
  (initial-placements (str column row)))

(def squares
  (into (sorted-map-by (fn [x y]
                         (let [[xrow xcol] (-> x position-to-coords vals)
                               [yrow ycol] (-> y position-to-coords vals)]
                           (compare [ycol xrow] [xcol yrow]))))
        (for [row (range 1 9)
              column (seq "abcdefgh")
              :let [position (str column row)
                    value (initial-placement column row)]]
          [position {:value value :position position}])))

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
