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
  (let [value (:value @from)
        from-val @from
        to-val @to]
    (when ((p/possible-moves from-val (peek @history))
           (:position to-val))
      (swap! history conj {:from from-val :to to-val})
      (om/update! app :selected nil)
      (om/update! from :value nil)
      (om/update! to :value value))))

(defn update-square! [app square]
  (let [selected (:selected @app)
        selecting? (:value @square)
        unselecting? (and selected (= @square @selected))]
    (cond
      unselecting? (om/update! app :selected nil)
      selected (move-piece! selected square app)
      selecting? (om/update! app :selected square))))

(defn rewind! [app]
  (when-let [prior-move (peek @history)]
    (om/transact! app :squares
                  (fn [history] (let [from (:from prior-move)
                                      to (:to prior-move)]
                                  (merge history {(:position from) from
                                                  (:position to) to}))))
    (swap! history pop)))


(defn square [{:keys [position value selected targetable] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (let [class-names (cond-> [position (:type value)]
                          targetable (conj "targetable")
                          selected (conj "selected"))]
        (dom/td #js {:onClick #(put! select-chan square)
                     :className  (apply str (interpose " " class-names))}
                (dom/a nil (b/icons (-> value vals set))))))))

(defn build-squares [app select-chan]
  (let [rows (partition 8 (-> app :squares vals))
        selected (:selected app)
        targets (p/possible-moves selected (peek @history))
        init (fn [{:keys [position] :as square}]
               (cond-> square
                 (targets position) (assoc :targetable true)
                 (= selected square) (assoc :selected true)))
        options {:key :position
                 :init-state {:select-chan select-chan}
                 :fn init}]
    (apply dom/table #js {:className "chess-board"}
           (map (fn [row] (apply dom/tr #js {:key (:position (first row))}
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
              (let [[square c] (alts! [select rewind])]
                (if (= c select)
                  (update-square! app square)
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
