(ns forking-chess.piece
  (:require [clojure.set :refer [map-invert]]))

(def column-to-int
  (zipmap (seq "abcdefgh")
          (range 8)))

(def int-to-column
  (map-invert column-to-int))

(defn position-to-coords [[column row]]
  [(column-to-int column)
   (-> row str js/parseInt dec)])

(defn coords-to-position [[x y]]
  (let [column (int-to-column x)
        row (inc y)]
    (when (and column (>= y 0) (< y 8))
      (str column
           (inc y)))))

(defn piece-to-coords [piece]
  (-> piece :position position-to-coords))

(defn en-passant [piece [x y] prior-move]
  (let [[from-x from-y] (position-to-coords (:from prior-move))
        [to-x to-y] (position-to-coords (:to prior-move))]
    (when (and (= (:type piece) :P)
               (= (get-in prior-move [:piece :type]) :P)
               (= to-y y)
               (= (Math/abs (- from-y to-y)) 2)
               (= (Math/abs (- to-x x)) 1))
      (cond
        (= (- from-y to-y)  2) [to-x (inc to-y)]
        (= (- from-y to-y) -2) [to-x (dec to-y)]))))

(def king-moves
  (let [ops [inc dec identity]]
    (for [op1 ops
          op2 ops
          :when (not= op1 op2 identity)]
      [(op1 0) (op2 0)])))

(def rook-moves
  (let [ops [+ -]
        moves (range 8)]
    (for [op1 ops
          op2 ops
          xmove moves
          ymove moves
          :when (not= xmove ymove 0)
          :when (or (= 0 xmove) (= 0 ymove))]
      [(op1 0 xmove) (op2 0 ymove)])))

(def bishop-moves
  (let [ops [+ -]
        moves (range 8)]
    (for [op1 ops
          op2 ops
          xmove moves
          ymove moves
          :when (not= xmove ymove 0)
          :when (= xmove ymove)]
      [(op1 0 xmove) (op2 0 ymove)])))

(def knight-moves
  (let [ops [+ -]
        moves [1 2]]
    (for [op1 ops
          op2 ops
          xmove moves
          ymove moves
          :when (not= xmove ymove)]
      [(op1 0 xmove) (op2 0 ymove)])))

(defn basic-moves [{:keys [color type]}]
  ({:K king-moves
    :Q (concat rook-moves bishop-moves)
    :R rook-moves
    :B bishop-moves
    :N knight-moves
    :P (if (= "black" color)
         [[0 -1]]
         [[0  1]])} type))

(defn apply-moves [[x y] moves]
  (map (fn [[movex movey]] [(+ movex x) (+ movey y)])
       moves))

(defn positional-moves [{:keys [type color]} [x y]]
  (when (= :P type)
    (let [op ({"white" + "black" -} color)
          initial-move? (or (and (= "white" color) (= 1 y))
                            (and (= "black" color) (= 6 y)))]
      (when initial-move?
        [[x (op y 2)]]))))

(defn stateful-moves [{:keys [type] :as piece} coords [prior-move]]
  [(en-passant piece coords prior-move)])

(defn default-moves [piece coords]
  (apply-moves coords (basic-moves piece)))

(defn moves [piece position board history]
  (let [coords (position-to-coords position)]
    (->> (concat (default-moves piece coords)
                 (positional-moves piece coords)
                 (stateful-moves piece coords history))
         (map coords-to-position)
         (remove nil?)
         (into #{}))))
