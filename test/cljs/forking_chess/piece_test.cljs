(ns forking-chess.piece-test
  (:require-macros [cemerick.cljs.test :refer [is deftest]])
  (:require [cemerick.cljs.test :as t]
            [forking-chess.piece :as p]))

(deftest en-passant
  (is (= #{"a6" "b6"}
         (p/moves {:color "white" :type :P}
                  "b5"
                  {}
                  [{:from {:position "a7" :value {:color "black" :type :P}}
                    :to {:position "a5" :value nil}}])))
  (is (= #{"b6" "c6"}
         (p/moves {:color "white" :type :P}
                  "b5"
                  {}
                  [{:from {:position "c7" :value {:color "black" :type :P}}
                    :to {:position "c5" :value nil}}])))
  (is (= #{"f3" "g3"}
         (p/moves {:color "black" :type :P}
                  "f4"
                  {}
                  [{:from {:position "g2" :value {:color "white" :type :P}}
                    :to {:position "g4" :value nil}}])))
  (is (= #{"f3" "e3"}
         (p/moves {:color "black" :type :P}
                  "f4"
                  {}
                  [{:from {:position "e2" :value {:color "white" :type :P}}
                    :to {:position "e4" :value nil}}]))))

(deftest king
  (is (= #{"h1" "g1" "g2" "h3" "g3"}
         (p/moves {:color "white" :type :K} "h2" {} []))))

(deftest queen
  (is (= #{"h1" "g1" "g2" "h3" "f2" "g3" "h4" "e2" "h5" "d2" "f4" "h6" "c2" "h7" "b2" "e5" "h8" "a2" "d6" "c7" "b8"}
         (p/moves {:color "white" :type :Q} "h2" {} []))))

(deftest rook
  (is (= #{"a2" "b2" "c2" "d2" "e2" "f2" "g2" "h1" "h3" "h4" "h5" "h6" "h7" "h8"}
         (p/moves {:color "white" :type :R} "h2" {} []))))

(deftest bishop
  (is (= #{"g1" "g3" "f4" "e5" "d6" "c7" "b8"}
          (p/moves {:color "white" :type :B} "h2" {} []))))

(deftest knight
  (is (= #{"f1" "f3" "g4"}
         (p/moves {:color "white" :type :N} "h2" {} [])))
  (is (= #{"f1" "f3" "g4"}
         (p/moves {:color "white" :type :N} "h2" {} []))))

(deftest pawn
  (is (= #{"a5" "a6"}
         (p/moves {:color "black" :type :P} "a7" {} [])))
  (is (= #{"h3" "h4"}
         (p/moves {:color "white" :type :P} "h2" {} [])))
  (is (= #{"h1"}
         (p/moves {:color "black" :type :P} "h2" {} []))))
