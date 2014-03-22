(ns forking-chess.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! alts! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [forking-chess.board :as b]
            [forking-chess.piece :as p]))

(enable-console-print!)

(def app-state (atom {:squares b/squares}))
(def history (atom '()))

(defn move-piece! [from to app]
  (let [board (:squares @app)
        piece (get board from)
        captured (get board to)
        moves (p/moves piece from board @history)]
    (when (moves to)
      (swap! history conj {:from from :to to :piece piece :captured captured})
      (om/update! app :selected nil)
      (om/update! app [:squares from] nil)
      (om/update! app [:squares to] piece))))

(defn update-square! [app square position]
  (let [selected (:selected @app)
        unselecting? (and selected (= position selected))]
    (cond
      unselecting? (om/update! app :selected nil)
      selected (move-piece! selected position app)
      :else (om/update! app :selected position))))

(defn rewind! [app]
  (when-let [prior-move (peek @history)]
    (om/transact! app :squares
                  (fn [history] (let [from (:from prior-move)
                                      to (:to prior-move)
                                      piece (:piece prior-move)
                                      captured (:captured prior-move)]
                                  (merge history {from piece
                                                  to captured}))))
    (swap! history pop)))


(defn square [{:keys [position type color selected targetable] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (let [class-names (cond-> [position type]
                          targetable (conj "targetable")
                          selected (conj "selected"))]
        (dom/td #js {:onClick #(put! select-chan [square position])
                     :className  (apply str (interpose " " class-names))}
                (dom/a nil (b/icons #{type color})))))))

(defn build-squares [app select-chan]
  (let [board (:squares app)
        rows (partition 8 board)
        selected-square (:selected app)
        selected-piece (get board selected-square)
        targets (p/moves selected-piece selected-square board @history)
        init (fn [[position piece]]
               (cond-> piece
                 true (assoc :position position) ; kludge
                 (targets position) (assoc :targetable true)
                 (= selected-square position) (assoc :selected true)))
        options {:key :position
                 :init-state {:select-chan select-chan}
                 :fn init}]
    (apply dom/table #js {:className "chess-board"}
           (map (fn [row] (apply dom/tr #js {:key (ffirst row)}
                                 (om/build-all square row options)))
                rows))))

(defn chess-board [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:select (chan)
       :rewind (chan)})
    om/IWillMount
    (will-mount [_]
      (let [select (om/get-state owner :select)
            rewind (om/get-state owner :rewind)]
        (go (while true
              (let [[message c] (alts! [select rewind])]
                (if (= c select)
                  (update-square! app (first message) (last message))
                  (rewind! app)))))))
    om/IRenderState
    (render-state [_ {:keys [selectable select rewind] :as state}]
      (dom/div nil
               (build-squares app select)
               (dom/button #js {:onClick #(put! rewind :t)} "Rewind")))))

(om/root
  chess-board
  app-state
  {:target (.getElementById js/document "content")})
