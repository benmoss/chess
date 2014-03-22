(ns forking-chess.piece
  (:require [forking-chess.utils :refer [position-to-coords
                                         coords-to-position]]))

;
; static move shapes
;

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

(defn default-moves [piece [x y]]
  (map (fn [[movex movey]] [(+ movex x) (+ movey y)])
       ({:K king-moves
         :Q (concat rook-moves bishop-moves)
         :R rook-moves
         :B bishop-moves
         :N knight-moves
         :P []} (:type piece))))

;
; pawn moves
;

(defn en-passant [pawn [x y] prior-move]
  (let [[from-x from-y] (position-to-coords (:from prior-move))
        [to-x to-y] (position-to-coords (:to prior-move))]
    (when (and (= (:type pawn) :P)
               (= (get-in prior-move [:piece :type]) :P)
               (= to-y y)
               (= (Math/abs (- from-y to-y)) 2)
               (= (Math/abs (- to-x x)) 1))
      (cond
        (= (- from-y to-y)  2) [[to-x (inc to-y)]]
        (= (- from-y to-y) -2) [[to-x (dec to-y)]]))))

(defn initial-move [{:keys [color]} [x y]]
  (let [op ({"white" + "black" -} color)
        initial-move? (or (and (= "white" color) (= 1 y))
                          (and (= "black" color) (= 6 y)))]
    (when initial-move?
      [[x (op y 2)]])))

(defn pawn-moves [pawn coords board history]
  (concat (en-passant pawn coords (peek history))
          (initial-move pawn coords)
          (if (= "black" (:color pawn))
            [[(first coords) (dec (last coords))]]
            [[(first coords) (inc (last coords))]])))

(defn contextual-moves [piece coords board history]
  (when (= :P (:type piece))
    (pawn-moves piece coords board history)))

(defn moves [piece position board history]
  (let [coords (position-to-coords position)]
    (->> (concat (default-moves piece coords)
                 (contextual-moves piece coords board history))
         (map coords-to-position)
         (remove nil?)
         (into #{}))))
