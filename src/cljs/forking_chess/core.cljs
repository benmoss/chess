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

(def base-row [:R :N :B :K :Q :B :N :R])

(def initial-placements
  (merge (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 8))
                 (partition 2 (interleave (repeat "black") base-row)))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 7))
                 (repeat '("black" :P)))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 2))
                 (repeat '("white" :P)))
         (zipmap (map #(keyword (str % %2)) (seq "abcdefgh") (repeat 1))
                 (partition 2 (interleave (repeat "white") base-row)))))

(defn initial-placement [column row]
  (initial-placements (keyword (str column row))))

(def squares
  (into (sorted-map-by #(compare (reverse (str %2)) (reverse (str %))))
        (for [row (range 1 9)
              column (seq "abcdefgh")
              :let [position (keyword (str column row))
                    value (initial-placement column row)]]
          [position {:value value :position position}])))

(def app-state (atom {:squares squares}))

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
      (let [rows (partition 8 (vals (:squares app)))
            options {:key :position
                     :init-state {:comm comm}}]
        (apply dom/table #js {:className "chess-board"}
               (map #(dom/tr #js {:key (:position (first %))}
                             (om/build-all square % options))
                    rows))))))

(defn click [e {:keys [value] :as square} owner comm]
  (when value
    (put! comm [:select square])))

(defn square [{:keys [position value] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm]}]
      (dom/td #js {:onClick (om/bind click square owner comm) :className position}
              (dom/a nil (icons value))))))

(om/root app-state chess-game (.getElementById js/document "content"))
