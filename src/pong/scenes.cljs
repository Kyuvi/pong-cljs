(ns pong.scenes
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
            ) )



(defn draw-game-scene [ctx state]
  (let [score (:score state )
        ball (get-in state [:scene :ball])
        pdl1 (get-in state [:scene :paddle-one])
        pdl2 (get-in state [:scene :paddle-two])
        [b-left b-right] ((juxt :left :right) kn/game-border)
        [b-top b-bot] ((juxt :top :bottom) kn/game-border)
        [b-width b-height] ((juxt :width :height) kn/game-border)
        ]
    (pr/draw-blk-line ctx b-left pr/margin-size b-right pr/margin-size)
    (pr/draw-blk-line ctx b-left b-bot b-right b-bot)
    (doseq [i (range (* 3.5 pr/grid-size) b-bot (* 2 pr/grid-size))]
      (pr/fill-block  ctx (- (/ (:width kn/game-view) 2) (/ pr/grid-size 2)) i))

    ;; sprites
    (run! #(obj/draw-sprite % ctx) [score ball pdl1 pdl2])))

(defn center-text
  "Returns the x position in a scene for text `txt` of size `size`."
  [txt size]
  (/ (- (:width kn/game-view) (* (count txt) size 4)) 2))

(defn draw-menu-scene [ctx state]
  (let [controls (get-in state [:settings :controls])
        p1-keys (str (:up (:p1 controls)) "/" (:down (:p1 controls)))
        p2-keys (str (:up (:p2 controls)) "/" (:down (:p2 controls)))
        t1 "PONG"
        ;; t2 "IN CLOJURESCRIPT"
        ;; t2 "[CLOJURESCRIPT]"
        t2 "IN CLJS"
        o1 "1P START"
        o2 "2P START"
        o3 "OPTIONS"
        o4 "CREDITS"
        b1 "ENTER - SELECT                   ESC - BACK   "
        b2 (str "PLAYER1 - " p1-keys
                "                    PLAYER2 - " p2-keys) ;; TODO: use settings
        b3 kn/version
        ;; cursor (rfu/<sub [::subs/cursor])
        cursor (:cursor state)
        [menu-size hint-size] [5 3]]
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt pr/grid-size) (* pr/grid-size ypos)
        txt ))
     [[t1 5] [t2 12]])
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt menu-size) (* pr/grid-size ypos)
        txt :size menu-size ))
     [[o1 22] [o2 27] [o3 32] [o4 37]])
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt hint-size)
        (- (:height kn/game-view) (* hint-size ypos)) txt :size hint-size ))
     [[b1 35] [b2 28] [b3 14]])
    (obj/draw-sprite cursor ctx )
    )
  )

(defn draw-credits-scene [ctx]
  (let [t1 "PONG IN CLJS"
        m1 "This is an attempt at pong using clojurescript."
        m2 "The AI enemy is vicious!"
        m3 "You can find more information about the project on it's"
        m4 "github page:"
        m5 "https://codeberg.org/Kyuvi/cljs-pong"
        m6 "Thanks to John McCarthy, Steve Russel and Rich Hickley"
        m7 "also thanks to Luxedo for his excellent javascript version."
        m8 "https://github.com/luxedo/pong-almost-from-scratch"
        m9 ""
        b1 "Copyright (C) 2023 Kyuvi"
        b2 ""
        b3 "This software is released under the GNU GPL3 license."
        [titlesize creditsize] [5 3]
        ]
    (lt/write-text ctx 50 (* pr/grid-size 5) t1 :size titlesize)
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx 50
        (+ (* pr/grid-size 5) (* titlesize 21) (* creditsize ypos))
        txt :size creditsize))
     [[m1 0] [m2 7] [m3 21] [m4 28] [m5 42] [m6 56] [m7 63] [m8 70] [m9 84]])
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx 50
        (- (:height kn/game-view) (* pr/grid-size 5) (* creditsize ypos))
        txt :size creditsize))
     [[b1 21] [b2 14] [b3 7]])
    )
  )


(defn draw-options-scene [ctx state]
  (let [settings (:settings state)
        t1 "OPTIONS"
        ;; t2 (str (rfu/<sub [::subs/rounds]))
        t2 (str (:rounds settings))
        o1 "ROUNDS"
        ;; o2  (str "AI " (kn/game-difficulty (rfu/<sub [::subs/difficulty])))
        o2  (str "AI " (:difficulty settings))
        o3 "MENU"
        b3 kn/version
        ;; cursor (rfu/<sub [::subs/cursor])
        cursor (:cursor state)
        [menu-size hint-size] [5 3]
        ]
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt pr/grid-size) (* pr/grid-size ypos)
        txt ))
     [[t1 10] [t2 22]])
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt menu-size) (* pr/grid-size ypos)
        txt :size menu-size ))
     [[o1 30] [o2 35] [o3 40]])
    (lt/write-text
     ctx (center-text b3 hint-size) (- (:height kn/game-view) (* hint-size 14))
     b3 :size hint-size)
    ))

(defn draw-end-scene [ctx state]
  (let [prev (rfu/<sub [::subs/previous])
        ;; prev-score  (:score prev)
        {:keys [p1 p2]}  (:score prev)
        ;; winner (if (> (:p1 prev-score) (:p2 prev-score)) 1 2)
        winner (if (> p1 p2) 1 2)
        t1  "GAMEOVER"
        t2 (if (= (:mode prev) :single)
                         (str  " YOU " (if (== winner 1) "WIN " "LOSE "))
                         (str " PLAYER " winner " WINs "))
        t3 (str p1 " "  p2)
        o1 "PLAY AGAIN"
        o2 "MENU"
        b3 kn/version
        ;; cursor (rfu/<sub [::subs/cursor])
        cursor (:cursor state)
        [menu-size hint-size] [5 3]
        ]
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt pr/grid-size) (* pr/grid-size ypos)
        txt ))
     [[t1 5] [t2 15]])
    (run!
     (fn [[txt ypos]]
       (lt/write-text
        ctx (center-text txt menu-size) (* pr/grid-size ypos)
        txt :size menu-size ))
     [[o1 30] [o2 35]])
    (lt/write-text
     ctx (center-text b3 hint-size) (- (:height kn/game-view) (* hint-size 14))
     b3 :size hint-size)
    (obj/draw-sprite cursor ctx )
    )
  )
