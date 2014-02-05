(ns forking-chess.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [forking-chess.crossovers.board :as b]
            [forking-chess.piece :as p]))

(enable-console-print!)

(def app-state (atom {:squares b/squares}))

(defn move-piece [{:keys [from to app]}]
  (let [value (:value @from)
        selectables (:selectables @app)]
    (when (selectables (:position @to))
      (om/update! app dissoc :selected :selectables)
      (om/update! from dissoc :state :value)
      (om/update! to assoc :value value))))

(defn select-piece [{:keys [piece app]}]
  (om/update! piece assoc :state "selected")
  (om/update! app assoc :selected piece)
  (om/update! app assoc :selectables (set (p/available-moves @piece))))

(defn select-square [app square]
  (if-let [selected (:selected @app)]
    (move-piece {:from selected :to square :app app})
    (select-piece {:piece square :app app})))

(defn handle-event [type app square]
  (case type
    :select (select-square app square)))

(defn click [e square owner comm]
  (put! comm [:select square]))

(defn square [{:keys [position value state selectable] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [comm]}]
      (dom/td #js {:onClick #(click % square owner comm)
                   :className (apply str (interpose " " [position state selectable]))}
              (dom/a nil (b/icons (-> value vals set)))))))

(defn chess-game [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [comm (chan)]
        (om/set-state! owner :comm comm)
        (go (while true
              (let [[type square] (<! comm)]
                (handle-event type app square))))))
    om/IRenderState
    (render-state [_ {:keys [selectable comm] :as state}]
      (let [rows (partition 8 (vals (:squares app)))
            options {:key :position
                     :init-state {:comm comm}
                     :fn (fn [square]
                           (if-let [selectables (:selectables app)]
                             (cond-> square
                               ((:selectables app) (:position square))
                               (assoc :selectable "selectable"))
                             square))}]
        (apply dom/table #js {:className "chess-board"}
               (map #(dom/tr #js {:key (:position (first %))}
                             (om/build-all square % options))
                    rows))))))

(om/root app-state chess-game (.getElementById js/document "content"))
