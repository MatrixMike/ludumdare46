(ns ^:figwheel-hooks ludumdare46.client
  (:require [rum.core :as rum]
            [goog.dom :as dom]
            [impi.core :as impi]))

(defmethod impi/update-prop! :pixi.object/pivot [object index _ pivot]
  (.. object -pivot (set (pivot 0) (pivot 1))))

(defn stage
  [t]
  (let [bg-count 20]
    {:impi/key                :state
     :pixi.object/type        :pixi.object.type/container
     :pixi.object/rotation    (/ t 1000)
     :pixi.object/position    (mapv + [600 300] [(* 100 (Math/sin (/ t 1000)))
                                                 (* 100 (Math/cos (/ t 1000)))])
     :pixi.object/scale       (let [s (Math/sin (/ t 1000))]
                                [(- (* 2 (inc (* s s)))
                                    0.5)
                                 (- (* 2 (inc (* s s)))
                                    0.5)])
     :pixi.object/pivot       [(* (/ bg-count 2) 64) (* (/ bg-count 2) 64)]
     :pixi.container/children `[~@(for [x (range bg-count)
                                        y (range bg-count)]
                                    {:impi/key             [x y]
                                     :pixi.object/type     :pixi.object.type/sprite
                                     :pixi.object/position [(* 64 x) (* 64 y)]
                                     :pixi.object/scale    [0.5 0.5]
                                     :pixi.sprite/texture  {:pixi.texture/source "assets/tile1.png"}})]}))

(defn impi-mount
  [state]
  (let [[system] (:rum/args state)]
    (impi/mount :canvas
                {:pixi/renderer  {:pixi.renderer/size             [1200 800]
                                  :pixi.renderer/background-color 0x000000}
                 :pixi/stage     (stage (:time @system))}
                (rum/dom-node state)))
  state)

(defn impi-update
  [state]
  (impi-mount state))

(defn animate
  [f]
  (let [stop (atom nil)]
    (js/requestAnimationFrame (fn cb [t]
                                (f t)
                                (when-not @stop
                                  (js/requestAnimationFrame cb))))

    #(reset! stop true) ;; return function to stop animation
    ))

(defn impi-unmount
  [state]

  (impi/unmount :canvas)
  state)

(def impi
  {:did-mount    impi-mount
   :will-update  impi-update
   :will-unmount impi-unmount})

(rum/defc canvas < rum/reactive, impi
  [system]
  (rum/react system)
  [:div])

(rum/defc page
  [system]
  [:div "ludumdare46"
   (canvas system)])

(defonce system
  (atom nil))

(defn main
  []
  (rum/mount (page system) (dom/getElement "app"))

  (when-let [stop (:stop @system)]
    (stop))

  (swap! system assoc :stop (animate (fn [t] (swap! system assoc :time t)))))

(defn ^:after-load reload
  []
  (main))
