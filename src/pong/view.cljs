(ns pong.view
  "Main canvas view of pong."
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require [reagent.core :as rg :refer [atom]]
            [reagent.dom :as rd]
            [re-frame.core :as rf]
            [sutils.canvas :as cvu]
            [sutils.rf :as rfu]
            [pong.prep :as pr]
            [pong.blkchars :as lt]
            [pong.obj :as obj]
            [pong.kone :as kn]
            [pong.rfm.subs :as subs]
            [pong.scenes :as scn]))


(defn pcanv []
  (let [update-view
        (fn [comp]
          (let
              [node (.-firstChild (rd/dom-node comp))
               ctx (.getContext node "2d")
               state (get (rg/props comp) :state)
               ;; mode (get (rg/props comp) :mode)
               ;; cursor (get (rg/props comp) :cursor)
               mode (:mode state)
               cursor (:cursor state)
               ]
            (case mode
              :menu
              ;; (scn/draw-menu-scene ctx (rfu/<sub [::subs/cursor]))
              (scn/draw-menu-scene ctx state)
              (:single :versus)
              (scn/draw-game-scene ctx state)
              :credits
              (scn/draw-credits-scene ctx)
              :end
              (scn/draw-end-scene ctx state)
              :else
              ;; (:single :versus)
              (do
                ;; (obj/draw-sprite
                ;; ;; ;; (println
                ;;  (rfu/<sub [::subs/cursor])
                ;;  ctx
                ;; )
                ;; (pr/fill-block ctx 400 40 20 :color "#FFF")
                (pr/fill-block ctx 0 0 20 :color "#FFF")
                ;; (pr/draw-blk-line ctx 40.5 50.5 45 400)
                ;; (pr/draw-blk-line ctx 40.1 50 400 50 :size 12)
                ;; (pr/draw-blk-line ctx 56.5 246 (+ 56.5 240) 246 )
                ;; (cvu/fill-square ctx (+ 400 5)  (+ 40 20)  10  )
                (cvu/fill-circle ctx 400 200 10)
                (cvu/fill-circle ctx (+ 400 (Math/sqrt 50)) (+ 200 (Math/sqrt 50)) 5)
            ;; (lt/write-text ctx 40 40 "hei")
                ))
            )
        )]
  (rg/create-class
   {
    :component-did-mount update-view
    ;; (fn [comp]
    ;;   (let
    ;;       [node (.-firstChild (rd/dom-node comp))
    ;;        ctx (.getContext node "2d")
    ;;         ;; state (:state @kn/app-state)
    ;;         mode (get (rg/props comp) :mode)
    ;;        ]
    ;;     (pr/fill-block ctx 400 40 20 :color "#FFF")
    ;;     (pr/fill-block ctx 0 0 20)
    ;;     (cvu/fill-square ctx (+ 400 5)  (+ 40 20)  10  )
    ;;     (cvu/fill-circle ctx 400 200 10)
    ;;     (cvu/fill-circle ctx (+ 400 (Math/sqrt 50)) (+ 200 (Math/sqrt 50)) 5)
    ;;     (lt/write-text ctx 40 40 "hei")
    ;;   )
    ;; )

    :component-did-update update-view
    ;; (fn [comp]
    ;;   (let [ctx (.getContext (.-firstChild (rd/dom-node comp)) "2d")
    ;;         ;; turtle (get (rg/props comp) :turtle)
    ;;         ]
    ;;     ;; (kn/tick!)
    ;;     ;; (println turtle)
    ;;     (obj/draw-turtle (:turtle @kn/app-state) ctx)
    ;;     ;; (obj/draw-turtle turtle ctx)
    ;;     ))

    :reagent-render
    (fn []
      [:div.game ;; {:style {:text-align "center"} :width 80}
       [:canvas {:id "pgcanv";; "game"
                 :width (:width pr/game-view)
                 :height (:height pr/game-view)
                 :style {:border "#FFF"
                         :border-style "solid"
                         :border-width "thick"
                 ;;        :position "relative"
                        :max-width "80%"
                        :max-height "80%"
                         :margin-top "2%"
                         }
                 }
        ]
       ]
    )})))

(defn pcanv-outer []
  (let [state (rfu/<sub [::subs/state])
        ;; mode (rfu/<sub [::subs/mode])
        ;; cursor (rfu/<sub [::subs/cursor])
        ] ;; NOTE: subscriptions need to be loader
    ;; [(pcanv) {:mode mode :cursor cursor}]))
    [(pcanv) {:state state}]))
