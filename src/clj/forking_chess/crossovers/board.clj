(ns forking-chess.crossovers.board
  (:require [clojure.string :as str]))

(def base-row [:R :N :B :K :Q :B :N :R])

(def initial-placements
  (merge (zipmap (map str (seq "abcdefgh") (repeat 8))
                 (map (partial hash-map :color "black" :type) base-row))
         (zipmap (map str (seq "abcdefgh") (repeat 7))
                 (repeat {:color "black" :type :P}))
         (zipmap (map str (seq "abcdefgh") (repeat 2))
                 (repeat {:color "white" :type :P}))
         (zipmap (map str (seq "abcdefgh") (repeat 1))
                 (map (partial hash-map :color "white" :type) base-row))))

(defn initial-placement [column row]
  (initial-placements (str column row)))

(def squares
  (into (sorted-map-by #(compare (str/reverse (str %2)) (str/reverse (str %))))
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

