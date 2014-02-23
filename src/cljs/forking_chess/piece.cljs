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

(defn en-passant [piece prior-move]
  (when (= (-> prior-move vals first :value :type) :P)
    (let [{:keys [x y]} (piece-to-coords piece)
          from (vals (position-to-coords (-> prior-move keys first)))
          to (vals (position-to-coords (-> prior-move keys last)))]
      (first (for [x-op [+ -]
                   y-op [+ -]
                   :when (and (= [(x-op x 1) (y-op y 2)] from)
                              (= [(x-op x 1) y] to))]
               (coords-to-position (x-op x 1) (y-op y 1)))))))

(defmethod possible-moves :P [piece prior-move]
  (let [{:keys [x y]} (piece-to-coords piece)
        color (get-in piece [:value :color])
        op ({"white" + "black" -} color)
        default (coords-to-position x (op y 1))
        initial-move? (or (and (= "white" color) (= 1 y))
                          (and (= "black" color) (= 6 y)))
        initial (coords-to-position x (op y 2))
        en-passant (en-passant piece prior-move)]
    (cond-> #{default}
      initial-move? (conj initial)
      en-passant (conj en-passant))))


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
  (en-passant {:position "b5" :value {:color "white" :type :P}}
                  {"a7" {:value {:color "black" :type :P}}
                   "a5" {:value nil}})
  (en-passant {:position "b5" :value {:color "white" :type :P}}
                  {"c7" {:value {:color "black" :type :P}}
                   "c5" {:value nil}})
  (en-passant {:position "f4" :value {:color "black" :type :P}}
                  {"g2" {:value {:color "white" :type :P}}
                   "g4" {:value nil}})
  (en-passant {:position "f4" :value {:color "black" :type :P}}
                  {"e2" {:value {:color "white" :type :P}}
                   "e4" {:value nil}})
  (possible-moves {:position "h2" :value {:color "white" :type :K}})
  (possible-moves {:position "h2" :value {:color "white" :type :Q}})
  (possible-moves {:position "h2" :value {:color "white" :type :R}})
  (possible-moves {:position "h2" :value {:color "white" :type :B}})
  (possible-moves {:position "h2" :value {:color "white" :type :N}})
  (possible-moves {:position "a7" :value {:color "black" :type :P}})
  (possible-moves {:position "h2" :value {:color "white" :type :P}})
  (possible-moves {:position "h2" :value {:color "black" :type :P}}))
