(ns forking-chess.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-http.client :as http]
            [forking-chess.utils :refer [guid]]))


(enable-console-print!)

(declare chess-board squares row square)

(def rows
  (vec (for [row (range 1 9)
        :let [columns (seq "abcdefgh")
              squares (vec (map #(assoc {} :row row :column %) columns))]]
         {:squares squares})))

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

(defn square [{:keys [row column]}]
  (om/component
    (dom/td #js {:id (str row column)}
            (dom/a nil (str row column)))))


(om/root app-state chess-game (.getElementById js/document "content"))
