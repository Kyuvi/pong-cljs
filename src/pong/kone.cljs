(ns pong.kone
  "Main engine of pong game."
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

(defn make-cursor-xs
  "Returns the vector of [x y] vectors for `mode`."
  [mode]
  (let [xpos-key (if (#{:menu :options :end} mode) :centered :left)
        xpos (xpos-key pr/cursor-xpos)
        y-pos-xs (mode pr/cursor-ypos)]
    ;; (when y-pos-xs
    (mapv (fn [ypos] [xpos (* pr/grid-size ypos)]) y-pos-xs)
    ;; )
  ))


(defn make-game-paddle [side key-up key-down]
  (let [xpos (if (= side :left)
               (+ (:left pr/game-border) pr/grid-size)
               (- (:right pr/game-border) pr/grid-size ))]
    (obj/make-paddle xpos (/ (:height pr/game-border) 2) (:len pr/paddle-vals) ;;obj/paddle-len
                     key-up key-down
                     (:top pr/game-border) (:bottom pr/game-border))))

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
     )))




(defn initialize-state
  ([mode] (initialize-state mode nil))
  ([mode previous-state]
   (assert (contains? #{:menu :single :versus ;; :options :controls
                        :credits :end} mode)
          (str "mode " mode
               " not one of ':menu :single :versus :options :credits :end'"))
  (let [settings (if previous-state
                  (:settings previous-state)
                  {:rounds 5 :difficulty 1
                   :controls {:p1 {:up "r" :down "d"} :p2 {:up  "h" :down "i"}}})
        controls (:controls settings)
        [pou pod] ((juxt :up :down) (:p1 controls))
        [ptu ptd] ((juxt :up :down) (:p2 controls))
        ]
    {:state
     {:mode mode
      :settings settings
      :cursor (when (#{:menu :options :end} mode)
                (obj/make-cursor (:width pr/cursor-vals) (:height pr/cursor-vals)
                                 (make-cursor-xs mode)
                               (:thickness pr/cursor-vals)))
      :scene (when (contains? #{:single :versus} mode)
               {:paddle-one (make-game-paddle :left pou pod) ;"r" "d");  82 68 )
                :paddle-two  (make-game-paddle :right ptu ptd) ; "h" "i") ;72 73 )
                :ball (spawn-ball), :paused false})
      :score (when (contains? #{:single :versus} mode) ;; TODO: move into scene?
                   (obj/make-score (- (/ (:width pr/game-border) 2) pr/letter-spacing)
                                   (+ (:top pr/game-border) (* 2 pr/grid-size)) 0 0))
      }
     :previous (when previous-state
                 {:mode (:mode previous-state) :score (:score previous-state)})
     :key-input {:pressed #{} :single #{}}
     }
    )
   ))





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
        ;; {state :state prev :previous } (initialize-state new-mode old-state)]
        {:keys [state  previous]} (initialize-state new-mode old-state)]
    ;; (assoc db :state (:state new-state) :previous (:previous new-state))))
  (println current-pos new-mode)
  ;; (assoc db :state state :previous prev)
  (assoc db :state state :previous previous)
  )
  ;; )
)


(defn remove-pressed-key [db key]
  (update-in db [:key-input :pressed] #(disj % key)))

(defn add-pressed-key [db key]
  (update-in db [:key-input :pressed] #(conj % key)))


(defn switch-pause-flag [db]
  (update-in db [:state :scene :paused] #(if % false true)))


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


(defn remove-pressed-key-cofx
  [{db :db} key]
  {:db (remove-pressed-key db key)})

(defn add-pressed-key-cofx
  [{db :db} key]
  {:db (add-pressed-key db key)})


(defn switch-pause-flag-cofx
  [{db :db}]
  {:db (switch-pause-flag db)})

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


(def game-down-actions
  (merge credit-down-actions
         {" " #(switch-pause-flag-cofx %)}))

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

(def down-actions-by-mode
  {:menu menu-down-actions
   :single game-down-actions
   :versus  game-down-actions
   :options nil
   :controls nil
   :credits credit-down-actions
   :end end-down-actions})

(defn make-down-actions-map [oup odwn tup tdwn]
  (let [single-down-actions (make-single-down-actions-map oup odwn)
        versus-down-actions (make-vs-down-actions-map
                             single-down-actions tup tdwn)]
  {:menu menu-down-actions
   ;; :single single-down-actions
   ;; :versus versus-down-actions
   :single game-down-actions
   :versus  game-down-actions
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
      (add-pressed-key-cofx cofx key) ;; gives cleaner response in game
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


        ;;;; game scene updates ;;;;

(defn paddle-ai [difficulty paddle ball-y]
  (let [diff-step (case difficulty
                    0 (/ (:step pr/paddle-vals) 3 )
                    1 (:step pr/paddle-vals)
                    2 (*  (:step pr/paddle-vals) 3 ))
        [px py] (obj/get-pos paddle)
        ;; [size top bottom] ((juxt :size :top :bottom) paddle)
        {:keys [size top bottom]} paddle
        center-delta (- (+ ball-y pr/grid-size) py (/ (:len pr/paddle-vals)
                                                      ;; obj/paddle-len
                                                      2))
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
  (cond (or (not (contains? #{:single :versus} (get-in db [:state :mode])))
            (get-in db [:state :scene :paused])
          ;; might not write "paused" in game as no change in db
          ;; (not (.hasFocus js/document))
            )
    ;; (do (println "db" (get-in db [:state :mode]))
        db
        ;; set pause flag if out of focus
        (not (.hasFocus js/document)) (assoc-in db [:state :scene :paused] true)
    ;; )
        :else
        (let [cur-state (:state db)
              ;; [scene settings] ((juxt :scene :settings) state)
              {:keys [scene settings]} cur-state
              score (:score cur-state)
              ;; settings (:settings state)
              ;; [difficulty rounds] ((juxt :difficulty :rounds) settings)
              {:keys [difficulty rounds]} settings
              [ball old-pd1 old-pd2] ((juxt :ball :paddle-one :paddle-two)
                                         scene)
              tball (obj/update-sprite ball)
              [bx by] (obj/get-pos tball)
              [b-speed b-dir b-top b-bot] ((juxt :speed :direction :top :bottom)
                                           tball)
              pd1 (obj/update-sprite old-pd1)
              pd2 (if (= (:mode cur-state) :single)
                    (paddle-ai difficulty old-pd2 by)
                    (obj/update-sprite old-pd2))
              [p1-y p2-y] (mapv #(second (obj/get-pos %)) [pd1 pd2])
              ;; settings (get-in db [:state :settings])
              ;; rounds (get-in db [:state :settings :rounds])

              ;; p2-y (if (= (:mode cur-state) :single)
              ;;        (paddle-ai difficulty old-pd2 by)
              ;;        old-p2-y)
              ;; pd2 (if-not (= p2-y old-p2-y)
              ;;       (assoc old-pd2 :pos (obj/make-spos ((obj/get-pos old-pd2) 0)
              ;;                                          p2-y))
              ;;       old-pd2)
              ]
          ;; (println "updated" (js/date.now))
          (cond
            ;; ball paddle-1 collision
            (and (<= bx (+ (:left pr/game-border) (* 2 pr/grid-size)))
                 (>= (+ by pr/grid-size) p1-y)
                 (<= by (+ p1-y (+ (:len pr/paddle-vals)
                                   ;; obj/paddle-len
                                   (* 2 pr/grid-size)))))
            (let [dy (- (* 0.8 (/ (- (+ by (/ pr/grid-size 2)) p1-y)
                                  (+ (:len pr/paddle-vals)
                                     ;; obj/paddle-len
                                     (* pr/grid-size 2)))) 0.4)
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
                 (<= by (+ p2-y (+ (:len pr/paddle-vals)
                                   ;; obj/paddle-len
                                   (* 2 pr/grid-size)))))
            (let [dy (- (* 0.8 (/ (- (+ by (/ pr/grid-size 2)) p2-y)
                                  (+(:len pr/paddle-vals)
                                    ;; obj/paddle-len
                                    (* pr/grid-size 2)))) 0.4)
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
                (assoc db :state
                       (merge cur-state
                              {:scene (merge scene {:ball (spawn-ball :two)
                                                    :paddle-one pd1
                                                    :paddle-two pd2})}
                              {:score (obj/update-sprite score :p1)})))
            (<= bx (* 2 pr/grid-size))
            (do (pr/play-score)
                (assoc db :state
                       (merge cur-state
                              {:scene (merge scene {:ball (spawn-ball :one)
                                                    :paddle-one pd1
                                                    :paddle-two pd2})}
                              {:score (obj/update-sprite score :p2)})))
            ;; game end
            (or (>= (:p1 score) rounds ) (>= (:p2 score) rounds ))
            ;; (let [{state :state prev :previous } (initialize-state :end cur-state)]
              ;; (assoc db :state state :previous prev))
            (let [{:keys [state previous]} (initialize-state :end cur-state)]
              (assoc db :state state :previous previous))

            :else ;; update paddles and ball
            ;; (do  (println "next" tball)
            (assoc-in db [:state :scene ]
                      (merge scene {:paddle-one pd1
                                    :paddle-two pd2 :ball tball}))
            ;; )

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
