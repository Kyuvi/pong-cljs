(ns pong.kone
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require ;; [clojure.string :as str]
   [pong.prep :as pr]
   [pong.obj :as obj]
   [re-frame.core :as rf]
   [sutils.rf :as rfu]
   [pong.rfm.subs :as subs]
  ))

;; (def version "v0.01")

;; (def game-view {:fps 60 :width 804 :height 600 })


;; (def game-modes #{:menu :single :versus :options :controls :credits :end })

;; (def game-difficulty ["easy" "medium" "unfair"])


        ;;;; game initialization ;;;;


;; (def game-border
;;   (let [top (+ pr/margin-size pr/grid-size)
;;         bottom (- (:height pr/game-view) pr/margin-size pr/grid-size )
;;         left pr/grid-size
;;         right (- (:width pr/game-view) (* pr/grid-size 2))]
;;     {:top top :bottom bottom :left left :right right
;;      :height (- bottom top) :width (- right left)}))

(defn make-game-paddle [side key-up key-down]
  (let [xpos (if (= side :left)
               (+ (:left pr/game-border) pr/grid-size)
               (- (:right pr/game-border) pr/grid-size ))]
    (obj/make-paddle xpos (/ (:height pr/game-border) 2) obj/paddle-len
                     key-up key-down (:top pr/game-border) (:bottom pr/game-border))))

(defn spawn-ball
  ([] (spawn-ball nil))
  ([side]
   (let [angle (cond (= side :one) (* (/ 1 3) (+ (* (js/Math.random) 2) 2) js/Math.PI)
                     (= side :two) (* (/ 1 3) (dec (* (js/Math.random) 2)) js/Math.PI)
                     :else (+ (* (/ 1 3) (dec (* (js/Math.random) 2)) js/Math.PI)
                              (*(- 1 (Math/random) Math/PI))))
         center (- (/ (:width pr/game-view) 2) (/ pr/grid-size 2))
         h (+ (* (Math/random) (- (:height pr/game-border) (* 3 pr/grid-size)))
              (:top pr/game-border) pr/grid-size)]
     (obj/make-ball center h 0 angle (:top pr/game-border)
                    (- (:bottom pr/game-border) pr/grid-size))
     ;; (rf/dispatch [::event/ball-speed])
     ;; (js/setTimeout (rf/dispatch [:pong-rfm-events/ball-speed]) 500)
     ;; (js/setTimeout (js/alert "test") 500)
     ;; dispatch speed 500 then 2000
     )))


(def cursor-vals {:width (* pr/grid-size 20) :height (* pr/grid-size 4)
                  :thickness 5})

(defn make-cursor-xs [y-pos-xs]
  "Return a vector of [x y] vectors if `y-pos-xs` is given.
   Otherwise returns nil."
  (let [xpos (/ (- (:width pr/game-view) (:width cursor-vals)
                   (* (:thickness cursor-vals) 2))
                2)]
    (when y-pos-xs
      (mapv (fn [ypos] [xpos (* pr/grid-size ypos)]) y-pos-xs))))

(def cursor-ypos {:menu [20.5 25.5 30.5 35.5]
                 :options [28.5 33.5 38.5]
                 :end [28.5 33.5]})

;; (def menu-cursor-pos (make-cursor-xs [20.5 25.5 30.5 35.5]))
;; (def options-cursor-pos (make-cursor-xs [28.5 33.5 38.5]))
;; (def end-cursor-pos (make-cursor-xs [28.5 35.5]))


(defn initialize-state
  ([mode] (initialize-state mode nil))
  ([mode previous-state]
  (assert (contains? #{:menu :single :versus :options :credits :end} mode)
          (str "mode " mode
               " not one of ':menu :single :versus :options :credits :end'"))
  ;; (let [[pd1 pd2 ball score]
  ;;       (if (contains? #{:single :versus} mode)
  ;;         [(make-game-paddle :left 82 68 ) (make-game-paddle :right 72 73 )
  ;;          (spawn-ball)
  ;;          (obj/make-score (- (/ (:width pr/game-border) 2) pr/letter-spacing)
  ;;                          (+ (:top pr/game-border) (* 2 pr/grid-size)) 0 0)]
  ;;         nil)]
    {:state
     {:mode mode
      :settings (if previous-state
                  (:settings previous-state)
                  {:rounds 5 :difficulty 1
                   :controls {:p1 {:up "r" :down "d"} :p2 {:up  "h" :down "i"}}})
      :cursor (when (#{:menu :options :end} mode)
                (obj/make-cursor (:width cursor-vals) (:height cursor-vals)
                                 (make-cursor-xs (mode cursor-ypos))
                               ;; (cond (= mode :menu) menu-cursor-pos
                               ;;       (= mode :options) options-cursor-pos
                               ;;       (= mode :end) end-cursor-pos
                               ;;       :else nil)
                               (:thickness cursor-vals)))
      :scene (when (contains? #{:single :versus} mode)
               {:paddle-one (make-game-paddle :left "r" "d");  82 68 )
                :paddle-two  (make-game-paddle :right "h" "i") ;72 73 )
                :ball (spawn-ball), :paused false})
      :score (when (contains? #{:single :versus} mode) ;; TODO: move into scene?
                   (obj/make-score (- (/ (:width pr/game-border) 2) pr/letter-spacing)
                                   (+ (:top pr/game-border) (* 2 pr/grid-size)) 0 0)
                   ;; (= mode :end)
                   ;; (if (nil? previous-state)
                   ;;   (throw (js/Error. "mode 'end' needs a previous-state supplied") )
                   ;;   (:score previous-state))
                   )
      }
     :previous (when previous-state
                 {:mode (:mode previous-state) :score (:score previous-state)})
     :key-input {:pressed #{} :single #{}}
     }
    ;; )
   ))


;; (def initial-state (initialize-state :menu))

;;         ;;;; game sounds ;;;;

;; (defn make-sound-element [sound-file-name]
;;   ;; (let [sound-folder  "./resources/public/audio/"]
;;   (let [sound-folder  "./audio/"] ;; start path from (index.)html file
;;     (new js/Audio (str sound-folder sound-file-name))))

;; (def wall-sound (make-sound-element "4391__noisecollector__pongblipf-5.wav"))
;; (def paddle-sound (make-sound-element "4390__noisecollector__pongblipf-4.wav"))
;; (def score-sound (make-sound-element
;;                   "333785__projectsu012__8-bit-failure-sound.wav"))
;; (def select-sound (make-sound-element "275896__n-audioman__coin02.wav"))

;; (defn sound-factory [audio start stop]
;;   ;; (if (.paused audio)
;;     (.play audio)
;;     (js/setTimeout (fn [] (.pause audio)
;;                      (set! (.-currentTime audio) start )) stop))
;;   ;; )

;; (defn play-wall [] (sound-factory wall-sound 100 100))
;; (defn play-paddle [] (sound-factory paddle-sound 100 100))
;; (defn play-score [] (sound-factory score-sound 0 200))
;; (defn play-select [] (sound-factory select-sound 0 200))



        ;;;; game functions ;;;;

(defn move-paddle [db paddle dir]
  ;; (let [paddle (rfu/<sub [(keyword "pong.rfm.subs" paddle)])
  ;;       ]
  ;; (println "paddle")
    (update-in db [:state :scene paddle ]  #(obj/update-sprite % dir ))
    ;; )
  )

(defn move-cursor [db dir]
  ;; (println "cursor" db dir)
  (if (= dir :up) (pr/play-wall) (pr/play-paddle))
  (update-in db [:state :cursor] #(obj/update-sprite % dir)))

(defn switch-mode
  ;; ([db]
  ;;  (pr/play-select)
  ;;  (let [{state :state prev :previous } (initialize-state :menu)]
  ;;    (assoc db :state state :previous prev)
  ;;    ))
  ;; (
   [db mode-vec]
  ;; (println "here")
  (pr/play-select)
  (let [old-state (rfu/<sub [::subs/state])
        ;; current-cur (rfu/<sub [::subs/current-cur])
        current-pos (get-in old-state [:cursor :current])
        temp-mode (if (vector? mode-vec) (mode-vec current-pos) mode-vec)
        ;; new-state (initialize-state new-mode old-state)] ;; TODO: destructure?
        new-mode (if (= :previous temp-mode)
                   (rfu/<sub [::subs/previous-mode]) temp-mode)
        {state :state prev :previous } (initialize-state new-mode old-state)]
    ;; (assoc db :state (:state new-state) :previous (:previous new-state))))
  (println current-pos new-mode)
  (assoc db :state state :previous prev)
  )
  ;; )
)

;; (defn ball-speed-inc [db]
;;   (let [speed (get-in db [:state :scene :ball :speed])
;;         half-speed (/ obj/ball-speed 2)
;;         new-speed (if (< speed half-speed) half-speed obj/ball-speed)]
;;     (assoc-in db [:state :scene :ball :speed] new-speed)))


(defn remove-pressed-key [db key]
  (update-in db [:key-input :pressed] #(disj % key)))

(defn add-pressed-key [db key]
  (update-in db [:key-input :pressed] #(conj % key)))


        ;;;; cofx functions ;;;;
(defn no-op [{db :db :as cofx}] {:db db})

(defn move-paddle-cofx
  [{db :db} paddle dir]
  {:db (move-paddle db paddle dir)}
  )

(defn move-cursor-cofx
  [{db :db} dir]
  {:db (move-cursor db dir)})

;; (defn move-cursor-db
;;   [db dir]
;;   {:db (move-cursor db dir)})

(defn switch-mode-cofx
  ;; ([{db :db}]
  ;;  {:db (switch-mode db)})
  ;; (
   [{db :db} mode-vec]
  {:db (switch-mode db mode-vec)})
;; )

;; (defn ball-speed-inc-cofx
;;   [{db :db}]
;;   {:db (ball-speed-inc db)})


(defn remove-pressed-key-cofx
  [{db :db} key]
  {:db (remove-pressed-key db key)})

(defn add-pressed-key-cofx
  [{db :db} key]
  {:db (add-pressed-key db key)})

        ;;;; keymaps ;;;;

(def cursor-screen-actions
  {"ArrowUp" #(move-cursor-cofx % :up)
   "ArrowDown" #(move-cursor-cofx % :down)})

(def menu-down-actions
  (merge cursor-screen-actions
         {"Enter" #(switch-mode-cofx % [:single :versus :options :credits])
   ;; (fn [cofx]
   ;;           (let [current-cur (rfu/<sub [::subs/current-cur])
   ;;                 new-mode ([:single :versus :options :credits] current-cur )]
   ;;             (switch-mode-cofx cofx new-mode)))
   }))
;; (def menu-down-actions
;;   {"ArrowUp" #(move-cursor-db % :up)
;;    "ArrowDown" #(move-cursor-cofx % :down)
;;    })

;; (def paddle-movement-fns
;;   [#(move-paddle-cofx % :paddle-one :up)
;;     #(move-paddle-cofx % :paddle-one :down)
;;    #(move-paddle-cofx % :paddle-two :up)
;;     #(move-paddle-cofx % :paddle-two :down)
;;    ])

(def credit-down-actions
  {"Escape" #(switch-mode-cofx % :menu)})

(def options-down-actions
  (merge cursor-screen-actions credit-down-actions
         {
         ;; {"Enter" #(switch-mode-cofx % [:previous :menu])
         }))

(def end-down-actions
  (merge cursor-screen-actions credit-down-actions
         {"Enter" #(switch-mode-cofx % [:previous :menu])
          "Escape" #(switch-mode-cofx % :menu)}))


;; (def single-down-actions
;;   (let [{up :up down :down} (rfu/<sub [::subs/p-one-keys])]
;;     (merge credit-down-actions
;;     {"r"  #(move-paddle-cofx % :paddle-one :up)
;;      "d"  #(move-paddle-cofx % :paddle-one :down)})))

(defn make-single-down-actions-map [up down]
    (merge credit-down-actions
    {up  #(move-paddle-cofx % :paddle-one :up)
     down  #(move-paddle-cofx % :paddle-one :down)}))

(defn make-vs-down-actions-map [single-map tup tdown]
    (merge single-map
    {tup  #(move-paddle-cofx % :paddle-two :up)
     tdown  #(move-paddle-cofx % :paddle-two :down)}))

;; (def versus-down-actions
;;   (let [{tup :up tdown :down} (rfu/<sub [::subs/p-one-keys])]
;;     (merge single-down-actions
;;     {"h"  #(move-paddle-cofx % :paddle-two :up)
;;      "i"  #(move-paddle-cofx % :paddle-two :down)} )))

;; (def down-actions-by-mode
;;   {:menu menu-down-actions
;;    :single single-down-actions
;;    :versus versus-down-actions
;;    :options nil
;;    :credits credit-down-actions
;;    :end end-down-actions})

(defn make-down-actions-map [oup odwn tup tdwn]
  (let [single-down-actions (make-single-down-actions-map oup odwn)
        versus-down-actions (make-vs-down-actions-map
                             single-down-actions tup tdwn)]
  {:menu menu-down-actions
   :single single-down-actions
   :versus versus-down-actions
   :options nil
   :controls nil
   :credits credit-down-actions
   :end end-down-actions}))

(defn handle-key-down
  [{db :db :as cofx} kw {:keys [event key shift alt] :as data}]
  (let [{pou :up pod :down} (rfu/<sub [::subs/p-one-keys])
        {ptu :up ptd :down} (rfu/<sub [::subs/p-two-keys])
        mode (:mode (:state db))
        ckey-set (if (= mode :versus) #{pou pod ptu ptd} #{pou pod})
        down-actions (make-down-actions-map pou pod ptu ptd)
        ;; action (get-in down-actions-by-mode [(:mode (:state db)) key ] no-op)]
        action (get-in down-actions [(:mode (:state db)) key ] no-op)]
    (println action)
    (if (ckey-set key)
      (add-pressed-key-cofx cofx key)
      (action cofx))))

(defn handle-key-up
  [{db :db :as cofx} kw {:keys [event key shift alt] :as data}]
  (remove-pressed-key-cofx cofx key)
  )


;; (defn handle-key-down
;;   [db kw {:keys [key shift alt] :as data}]
;;   (let [action (get-in down-actions-by-mode [(:mode db) key ] no-op)]
;;     (println "kone key")
;;     (action db)))


(defn paddle-ai [difficulty paddle ball-y]
  (let [diff-step (case difficulty
                    0 (/ obj/paddle-step 3 )
                    1 obj/paddle-step
                    2 (*  obj/paddle-step 3 ))
        [px py] (obj/get-pos paddle)
        [size top bottom] ((juxt :size :top :bottom) paddle)
        center-delta (- (+ ball-y pr/grid-size) py (/ obj/paddle-len 2))
        temp-paddle-y (if (> (Math/abs center-delta) diff-step)
                        (+ py (/ diff-step (if (pos? center-delta) 1 -1)))
                        (+ py center-delta))
        new-paddle-y (cond (<= temp-paddle-y top) top
                           (>= (+ temp-paddle-y size) bottom) (- bottom size)
                           :else temp-paddle-y)
        ]
      (assoc paddle :pos (obj/make-spos px new-paddle-y))
    ;; new-paddle-y
    ;; diff-step
    ;; center-delta
    ))

(defn update-scene [db]
  ;; (println "updated" (js/Date.now))
  (if-not (contains? #{:single :versus} (get-in db [:state :mode]))
    ;; (do (println "db" (get-in db [:state :mode]))
    db
    ;; )
    (let [state (:state db)
          [scene settings] ((juxt :scene :settings) state)
          score (:score state)
          ;; settings (:settings state)
          [difficulty rounds] ((juxt :difficulty :rounds) settings)
          [ball old-pd1 old-pd2 ps] ((juxt :ball :paddle-one :paddle-two :paused)
                                 scene)
          tball (obj/update-sprite ball)
          [bx by] (obj/get-pos tball)
          [b-speed b-dir b-top b-bot] ((juxt :speed :direction :top :bottom)
                                       tball)
          pd1 (obj/update-sprite old-pd1)
          pd2 (if (= (:mode state) :single)
                 (paddle-ai difficulty old-pd2 by)
                 (obj/update-sprite old-pd2))
          [p1-y p2-y] (mapv #(second (obj/get-pos %)) [pd1 pd2])
          ;; settings (get-in db [:state :settings])
          ;; rounds (get-in db [:state :settings :rounds])

          ;; p2-y (if (= (:mode state) :single)
          ;;        (paddle-ai difficulty old-pd2 by)
          ;;        old-p2-y)
          ;; pd2 (if-not (= p2-y old-p2-y)
          ;;       (assoc old-pd2 :pos (obj/make-spos ((obj/get-pos old-pd2) 0)
          ;;                                          p2-y))
          ;;       old-pd2)
          ]
      ;; (println "updated" (js/date.now))
      ;; set pause flag if out of focus
      (cond
        ;; ball paddle-1 collision
        (and (<= bx (+ (:left pr/game-border) (* 2 pr/grid-size)))
             (>= (+ by pr/grid-size) p1-y)
             (<= by (+ p1-y (+ obj/paddle-len (* 2 pr/grid-size)))))
        (let [dy (- (* 0.8 (/ (- (+ by (/ pr/grid-size 2)) p1-y)
                              (+ obj/paddle-len (* pr/grid-size 2)))) 0.4)
              angle (* dy Math/PI)]
          (pr/play-paddle)
          ;; (println "paddle-one")
          ;; (update-in db [:scene :ball]
          ;; (assoc tball :direction angle :speed (+ b-speed 0.5))))
          (assoc-in db [:state :scene]
                    (merge scene
                           {:paddle-one pd1 :paddle-two pd2
                            :ball (assoc tball :direction angle
                                         :speed (+ b-speed 0.5))})))
        ;; ball paddle-1 collision
        (and (>= bx (- (:right pr/game-border) (* 2 pr/grid-size)))

             (>= (+ by pr/grid-size) p2-y)
             (<= by (+ p2-y (+ obj/paddle-len (* 2 pr/grid-size)))))
        (let [dy (- (* 0.8 (/ (- (+ by (/ pr/grid-size 2)) p2-y)
                              (+ obj/paddle-len (* pr/grid-size 2)))) 0.4)
              angle (* (- 1 dy) Math/PI)]
          (pr/play-paddle)
          ;; (println "paddle-two")
          ;; (assoc-in db [:scene :ball]
          ;; (assoc tball :direction angle :speed (+ b-speed 0.5))))
          (assoc-in db [:state :scene]
                    (merge scene
                           {:paddle-one pd1 :paddle-two pd2
                            :ball (assoc tball :direction angle
                                         :speed (+ b-speed 0.5))})))
        ;; score and respawn
        (>= bx (- (:width pr/game-view) (* 2 pr/grid-size)))
        (do (pr/play-score)
            (assoc db :state (merge state
                                    {:scene (merge scene
                                                   {:ball (spawn-ball :two)
                                                    :paddle-one pd1
                                                    :paddle-two pd2})}
                                    {:score (obj/update-sprite score :p1)})))
        (<= bx (* 2 pr/grid-size))
        (do (pr/play-score)
            (assoc db :state (merge state
                                    {:scene (merge scene
                                                   {:ball (spawn-ball :one)
                                                    :paddle-one pd1
                                                    :paddle-two pd2})}
                                    {:score (obj/update-sprite score :p2)})))
        ;; game end
        (or (>= (:p1 score) rounds ) (>= (:p2 score) rounds ))
        (let [{state :state prev :previous } (initialize-state :end state)]
          (assoc db :state state :previous prev))
        :else
        (do ;; (println
             ;; "next" tball)
            (assoc-in db [:state :scene ]
                      (merge scene {:paddle-one pd1
                                    :paddle-two pd2 :ball tball}))
)

        ;;
        ))
    ;; (update-in db [:state :scene :ball] #(obj/update-sprite %) )
    ))

(defn update-scene-cofx
  [{db :db}]
  {:db (update-scene db)})

(defn game-tick [{db :db :as cofx}]
  ;; (:db (update-scene db)))
  ;; (println "tick")
  ;; (:db (update-scene-cofx cofx))
  (update-scene-cofx cofx)
  )
