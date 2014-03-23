(ns forking-chess.board
  (:require [clojure.string :as str]
            [forking-chess.utils :refer [position-to-coords]]))

(enable-console-print!)

(def base-column {"a" "r"
                  "b" "n"
                  "c" "b"
                  "d" "q"
                  "e" "k"
                  "f" "b"
                  "g" "n"
                  "h" "r"})

(def initial-placements
  (into {}
        (for [row [1 2 7 8]
              column (map str (seq "abcdefgh"))
              :let [color (if (< row 5) "w" "b")
                    type (if (or (= 1 row)
                                 (= 8 row))
                           (base-column column) "p")]]
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
  {{:color "w" :type "k"} \♔
   {:color "w" :type "q"} \♕
   {:color "w" :type "r"} \♖
   {:color "w" :type "b"} \♗
   {:color "w" :type "n"} \♘
   {:color "w" :type "p"} \♙
   {:color "b" :type "k"} \♚
   {:color "b" :type "q"} \♛
   {:color "b" :type "r"} \♜
   {:color "b" :type "b"} \♝
   {:color "b" :type "n"} \♞
   {:color "b" :type "p"} \♟})
