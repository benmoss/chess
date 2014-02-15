(ns forking-chess.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [cljs.core.async :refer [put! alts! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [forking-chess.crossovers.board :as b]
            [forking-chess.piece :as p]))

(enable-console-print!)

(def app-state (atom {:squares b/squares :moves []}))

(defn move-piece [{:keys [from to app]}]
  (let [value (:value @from)
        selectables (:selectables @app)]
    (when (selectables (:position @to))
      (om/update! app dissoc :selected :selectables)
      (om/update! from dissoc :state :value)
      (om/update! to assoc :value value)
      (om/transact! app :moves #(conj % [(:position @from) (:position @to)])))))

(defn highlight-square [{:keys [square app]}]
  (om/update! square assoc :state "selected")
  (om/update! app assoc :selected square)
  (om/update! app assoc :selectables (set (p/available-moves @square))))

(defn unhighlight-square [{:keys [square app]}]
  (om/update! square dissoc :state)
  (om/update! app dissoc :selected :selectables))

(defn select-square [app square]
  (if-let [selected (:selected @app)]
    (if (= @square @selected)
      (unhighlight-square {:square square :app app})
      (move-piece {:from selected :to square :app app}))
    (highlight-square {:square square :app app})))

(defn square [{:keys [position value state selectable] :as square} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (dom/td #js {:onClick #(put! select-chan square)
                   :className (apply str (interpose " " [position state selectable]))}
              (dom/a nil (b/icons (-> value vals set)))))))

(defn build-squares [app select-chan]
  (let [rows (partition 8 (-> app :squares vals))
        init (fn [{:keys [position] :as square}]
               (cond-> square
                 (get (:selectables app) position) (assoc :selectable "selectable")))
        options {:key :position
                 :init-state {:select-chan select-chan}
                 :fn init}]
    (apply dom/table #js {:className "chess-board"}
           (map #(dom/tr #js {:key (:position (first %))}
                         (om/build-all square % options))
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
                  (select-square app square)
                  (rewind app)))))))
    om/IRenderState
    (render-state [_ {:keys [selectable select rewind] :as state}]
      (dom/div nil
               (build-squares app select)
               (dom/button #js {:onClick #(put! rewind)} "Rewind")))))

(om/root app-state chess-board (.getElementById js/document "content"))
