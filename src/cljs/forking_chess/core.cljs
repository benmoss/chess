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
              squares (vec (map #(assoc {:row row
                                         :value (initial-placement % row)
                                         :id (guid)}
                                        :column %)
                                columns))]]
         {:squares squares :id (guid)})))

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

(defn select-square [app {:keys [id]}]
  (println app))

(defn handle-event [type app val]
  (case type
    :select (select-square app val)))

(defn chess-game [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [comm (chan)]
        (om/set-state! owner :comm comm)
        (go (while true
              (let [[type value] (<! comm)]
                (om/read value #(handle-event type app %)))))))
    om/IRenderState
    (render-state [_ {:keys [editing comm] :as state}]
      (dom/div nil
               (om/build chess-board app
                         {:init-state {:comm comm}})))))

(defn chess-board [{:keys [rows]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm] :as state}]
      (dom/table #js {:className "chess-board"}
                 (om/build-all row rows {:key :id
                                         :init-state {:comm comm}})))))

(defn row [{:keys [squares]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm] :as state}]
      (dom/tr nil
              (om/build-all square squares {:key :id
                                            :init-state {:comm comm}})))))

(defn click [e {:keys [value] :as square} owner comm]
  (when value
    (put! comm [:select square])))

(defn square [{:keys [row column value] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm]}]
      (dom/td #js {:onClick (om/bind click square owner comm)}
              (dom/a nil (icons value))))))

(om/root app-state chess-game (.getElementById js/document "content"))
