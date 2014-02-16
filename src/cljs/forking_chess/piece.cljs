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

(defmulti possible-moves #(get-in % [:value :type]))

(defmethod possible-moves :P [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        color (get-in piece [:value :color])
        op ({"white" + "black" -} color)
        default (coords-to-position x (op y 1))
        initial (coords-to-position x (op y 2))]
    (if (or (and (= "white" color) (= 1 y))
            (and (= "black" color) (= 6 y)))
      (move-set [default initial])
      (move-set [default]))))

(defmethod possible-moves :K [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        ops [inc dec identity]]
    (move-set (for [op1 ops
                    op2 ops
                    :when (not= op1 op2 identity)]
                (coords-to-position (op1 x) (op2 y))))))

(defmethod possible-moves :Q [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        ops [+ -]
        moves (range 8)]
    (move-set (for [op1 ops
                    op2 ops
                    xmove moves
                    ymove moves
                    :when (not= xmove ymove 0)
                    :when (or (or (= 0 xmove) (= 0 ymove))
                              (= xmove ymove))]
                (coords-to-position (op1 x xmove) (op2 y ymove))))))

(defmethod possible-moves :R [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        ops [+ -]
        moves (range 8)]
    (move-set (for [op1 ops
                    op2 ops
                    xmove moves
                    ymove moves
                    :when (not= xmove ymove 0)
                    :when (or (= 0 xmove) (= 0 ymove))]
                (coords-to-position (op1 x xmove) (op2 y ymove))))))

(defmethod possible-moves :B [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        ops [+ -]
        moves (range 8)]
    (move-set (for [op1 ops
                    op2 ops
                    xmove moves
                    ymove moves
                    :when (not= xmove ymove 0)
                    :when (= xmove ymove)]
                (coords-to-position (op1 x xmove) (op2 y ymove))))))

(defmethod possible-moves :N [piece]
  (let [{:keys [x y]} (piece-to-coords piece)
        ops [+ -]
        moves [1 2]]
    (move-set (for [op1 ops
                    op2 ops
                    xmove moves
                    ymove moves
                    :when (not= xmove ymove)]
                (coords-to-position (op1 x xmove) (op2 y ymove))))))

(defmethod possible-moves :default [_] #{})

;;;;;;;;;;;;;
(comment
  (possible-moves {:position "h2" :value {:color "white" :type :K}})
  (possible-moves {:position "h2" :value {:color "white" :type :Q}})
  (possible-moves {:position "h2" :value {:color "white" :type :R}})
  (possible-moves {:position "h2" :value {:color "white" :type :B}})
  (possible-moves {:position "h2" :value {:color "white" :type :N}})
  (possible-moves {:position "a7" :value {:color "black" :type :P}})
  (possible-moves {:position "h2" :value {:color "white" :type :P}})
  (possible-moves {:position "h2" :value {:color "black" :type :P}}))
