(ns forking-chess.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-http.client :as http]
            [forking-chess.utils :refer [guid]]))


(enable-console-print!)

(declare chess-board row square)

(def initial-placements
  (merge (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 8))
                 (map #(conj (list %) "black") [:R :N :B :K :Q :B :N :R]))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 7))
                 (repeat '("black" :P)))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 2))
                 (repeat '("white" :P)))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 1))
                 (map #(conj (list %) "white") [:R :N :B :K :Q :B :N :R]))))

(defn initial-placement [column row]
  (initial-placements (keyword (str column row))))

(def rows
  (vec (for [row (range 1 9)
        :let [columns (seq "abcdefgh")
              squares (vec (map #(assoc {} :row row :column % :value (initial-placement % row)) columns))]]
         {:squares squares})))

(def icons
  {["white" :K] \♔
   ["white" :Q] \♕
   ["white" :R] \♖
   ["white" :B] \♗
   ["white" :N] \♘
   ["white" :P] \♙
   ["black" :K] \♚
   ["black" :Q] \♛
   ["black" :R] \♜
   ["black" :B] \♝
   ["black" :N] \♞
   ["black" :P] \♟})


(def app-state (atom {:rows rows}))

(defn chess-game [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (om/build chess-board app)))))

(defn chess-board [{:keys [rows]}]
  (om/component
    (dom/table #js {:className "chess-board"}
               (om/build-all row rows))))

(defn row [{:keys [squares]}]
  (om/component
    (dom/tr nil
            (om/build-all square squares))))

(defn square [{:keys [row column value]}]
  (om/component
    (dom/td nil
            (dom/a nil (icons value)))))


(om/root app-state chess-game (.getElementById js/document "content"))
