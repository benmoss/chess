(ns forking-chess.piece-test
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [forking-chess.piece :refer [possible-moves]]))

(deftest en-passant
  (is (= #{"a6" "b6"}
         (possible-moves {:position "b5" :value {:color "white" :type :P}}
                         {:from {:position "a7" :value {:color "black" :type :P}}
                          :to {:position "a5" :value nil}})))
  (is (= #{"b6" "c6"}
         (possible-moves {:position "b5" :value {:color "white" :type :P}}
                         {:from {:position "c7" :value {:color "black" :type :P}}
                          :to {:position "c5" :value nil}})))
  (is (= #{"f3" "g3"}
         (possible-moves {:position "f4" :value {:color "black" :type :P}}
                         {:from {:position "g2" :value {:color "white" :type :P}}
                          :to {:position "g4" :value nil}})))
  (is (= #{"f3" "e3"}
         (possible-moves {:position "f4" :value {:color "black" :type :P}}
                         {:from {:position "e2" :value {:color "white" :type :P}}
                          :to {:position "e4" :value nil}}))))

(deftest king
  (is (= #{"h1" "g1" "g2" "h3" "g3"}
         (possible-moves {:position "h2" :value {:color "white" :type :K}}))))

(deftest queen
  (is (= #{"h1" "g1" "g2" "h3" "f2" "g3" "h4" "e2" "h5" "d2" "f4" "h6" "c2" "h7" "b2" "e5" "h8" "a2" "d6" "c7" "b8"}
         (possible-moves {:position "h2" :value {:color "white" :type :Q}}))))

(deftest rook
  (is (= #{"a2" "b2" "c2" "d2" "e2" "f2" "g2" "h1" "h3" "h4" "h5" "h6" "h7" "h8"}
         (possible-moves {:position "h2" :value {:color "white" :type :R}}))))

(deftest bishop
  (is (= #{"g1" "g3" "f4" "e5" "d6" "c7" "b8"}
         (possible-moves {:position "h2" :value {:color "white" :type :B}}))))

(deftest knight
  (is (= #{"f1" "f3" "g4"}
         (possible-moves {:position "h2" :value {:color "white" :type :N}})))
  (is (= #{"f1" "f3" "g4"}
         (possible-moves {:position "h2" :value {:color "white" :type :N}}))))

(deftest pawn
  (is (= #{"a5" "a6"}
         (possible-moves {:position "a7" :value {:color "black" :type :P}})))
  (is (= #{"h3" "h4"}
         (possible-moves {:position "h2" :value {:color "white" :type :P}})))
  (is (= #{"h1"}
         (possible-moves {:position "h2" :value {:color "black" :type :P}}))))
