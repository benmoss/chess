(ns forking-chess.piece
  (:require [clojure.set :refer [map-invert]]))

(def column-to-int
  (zipmap (seq "abcdefgh")
          (range 8)))

(def int-to-column
  (map-invert column-to-int))

(defn position-to-coords [position]
  {:x (-> position first column-to-int)
   :y (-> position last str js/parseInt dec)})

(defn coords-to-position [x y]
  (let [column (int-to-column x)
        row (inc y)]
    (when (and column (>= y 0) (< y 8))
      (str column
           (inc y)))))

(defn piece-to-coords [piece]
  (-> piece :position position-to-coords))

(defn move-set [moves]
  (into #{}
        (remove nil? moves)))

(defmulti available-targets #(get-in % [:value :type]))

(defmethod available-targets :P [pawn]
  (let [{:keys [x y]} (piece-to-coords pawn)
        color (get-in pawn [:value :color])
        op ({"white" inc "black" dec} color)]
    (move-set [(coords-to-position x (op y))])))

(defmethod available-targets :K [king]
  (let [{:keys [x y]} (piece-to-coords king)
        ops [inc dec identity]]
    (move-set (for [op1 ops
                    op2 ops
                    :when (not= op1 op2 identity)]
                (coords-to-position (op1 x) (op2 y))))))

(defmethod available-targets :N [knight]
  (let [{:keys [x y]} (piece-to-coords knight)
        ops [+ -]
        moves [1 2]]
    (move-set (for [op1 ops
                    op2 ops
                    xmove moves
                    ymove moves
                    :when (not= xmove ymove)]
                (coords-to-position (op1 x xmove) (op2 y ymove))))))

(defmethod available-targets :default [_] #{})

;;;;;;;;;;;;;
(comment
  (available-targets {:position "h2" :value {:color "white" :type :K}})
  (available-targets {:position "h2" :value {:color "white" :type :N}})
  (available-targets {:position "a7" :value {:color "black" :type :P}})
  (available-targets {:position "h2" :value {:color "white" :type :P}})
  (available-targets {:position "h1" :value {:color "black" :type :P}}))
