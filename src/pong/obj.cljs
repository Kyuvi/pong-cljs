(ns pong.obj
  "Object(record) definitions and protocols of pong game"
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require [sutils.rf :as rfu]
            [pong.prep :as pr]
            [pong.blkchars :as lt]
            [pong.rfm.subs :as subs]))

;; (def ball-speed 10)
;; (def paddle-step 10 )
;; (def paddle-len (* 8 pr/grid-size) )

(defprotocol SpriteProtocol
(get-pos [sp] "Return a vector [x y] of co-ordinates of `sp`.")
(draw-sprite [sp ctx] "Draw sprite `sp` to canvas with context `ctx`.")
(update-sprite [sp]
               [sp input]
               "Update sprite `sp`, optionally based on `input`.")
  )


(defrecord SPos [x y]
  SpriteProtocol
  (get-pos
   [sp] [(:x sp) (:y sp)])
  )

(defn make-spos [x y]
  (SPos. x y))

(defrecord Score [pos p1 p2]
    SpriteProtocol
  (get-pos [sp] (get-pos (:pos sp)))
  (draw-sprite [sp ctx]
    (let [[x y] (get-pos sp)
          ;; [p1 p2] ((juxt :p1 :p2) sp)
          {:keys [p1 p2]} sp
          offset (dec (count (str p1)))]
      (lt/write-text ctx (- x (* 4 pr/grid-size offset)) y (str p1 " " p2))
      ))
  (update-sprite [sp pl-key]
    (update sp pl-key inc))
  )

(defn make-score [x y pl-one pl-two]
  (Score. (make-spos x y) pl-one pl-two))

(defrecord Paddle [pos size key-up key-down top bottom]
  SpriteProtocol
  (get-pos
    [sp] (get-pos (:pos sp)))
  (draw-sprite [sp ctx]
    (let [[x y] (get-pos sp)
          size (:size sp)]
    (pr/draw-blk-line ctx x y x (+ y size) )))
(update-sprite [sp]
    (let [[x y] (get-pos sp)
          pressed-set (rfu/<sub [::subs/pressed])
          [kup kdn] ((juxt :key-up :key-down) sp)
          ;; [size top bottom] ((juxt :size :top :bottom) sp)
          {:keys [size top bottom]} sp
          ;; nx (if (.-isDown kup) (- y (:step pr/paddle-vals)) )
          ty (cond (pressed-set kup) (- y (:step pr/paddle-vals))
                   (pressed-set kdn) (+ y (:step pr/paddle-vals))
                   :else y)
          ny (cond (<= ty top) top
                   (>= (+ ty size) bottom) (- bottom size)
                   :else ty)
                   ]
      ;; (when-not (== y ny)
      (assoc sp :pos (make-spos x ny))
      ;; )
   ) )
  (update-sprite [sp dir]
    (let [[x y] (get-pos sp)
          ;; [size top bottom] ((juxt :size :top :bottom) sp)
          {:keys [size top bottom]} sp
          ty (cond (= dir :up) (- y (:step pr/paddle-vals))
                   (= dir :down) (+ y (:step pr/paddle-vals))
                   :else y)
          ny (cond (<= ty top) top
                   (>= (+ ty size) bottom) (- bottom size)
                   :else ty)
                   ]
      ;; (when-not (== y ny)
      (assoc sp :pos (make-spos x ny))
      ;; )
   ) ))

(defn make-paddle [x y size key-up key-dn top bottom]
  (Paddle. (make-spos x y) size key-up key-dn top bottom))


(defn make-game-paddle
  [side key-up key-down]
  (let [xpos (if (= side :left)
               (+ (:left pr/game-border) pr/grid-size)
               (- (:right pr/game-border) pr/grid-size ))]
    (make-paddle xpos (/ (:height pr/game-border) 2) (:len pr/paddle-vals) ;;obj/paddle-len
                     key-up key-down
                     (:top pr/game-border) (:bottom pr/game-border))
    ;; (Paddle. (make-spos xpos (/ (:height pr/game-border) 2))
    ;;          (:len pr/paddle-vals)
    ;;          key-up key-down
    ;;          (:top pr/game-border) (:bottom pr/game-border))
    ))


(defrecord Ball [pos speed direction top bottom tick-count]
  SpriteProtocol
  (get-pos [sp] (get-pos (:pos sp)))
  (draw-sprite [sp ctx]
    (let [[x y] (get-pos sp)]
      (pr/fill-block ctx x y)))
  (update-sprite [sp]
    (let [[x y] (get-pos sp)
          [speed dir top bot] ((juxt :speed :direction :top :bottom) sp)
          count (:tick-count sp)
          [tx ty] [(+ x (* speed (Math/cos dir))) (+ y (* speed (Math/sin dir)))]
          edge-pred (or (>= ty bot) (<= ty top) )
          ndir (if edge-pred (* dir -1) dir)
          [nx ny] (if edge-pred
                    [(+ tx ( * speed (Math/cos ndir)))
                     (+ ty (* speed (Math/sin ndir)))]
                    [tx ty])
          new-count (when (and count (< count 200)) (inc count))
          new-speed (cond (and count (< count 80)) (/ pr/ball-speed 2)
                          (and count (< count 160) (< speed pr/ball-speed)) pr/ball-speed
                          :else speed)
          ]
      (when edge-pred (pr/play-wall))
      (assoc sp :pos (make-spos nx ny) :speed new-speed :direction ndir
             :tick-count new-count)
      )))

(defn make-ball [x y speed direction top bottom]
  (Ball. (make-spos x y) speed direction top bottom 0))

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
     (make-ball center h 0 angle (:top pr/game-border)
                    (- (:bottom pr/game-border) pr/grid-size))
     )))

(defrecord Cursor [w h pos-xs size color current timeout]
  SpriteProtocol
  (draw-sprite [sp ctx]
    (let [
          ;; [w h size color] ((juxt :w :h :size :color) sp)
          ;; pos-xs (:pos-xs sp)
          ;; current (:current sp)
          {:keys [w h size color pos-xs current]} sp
          [x y] (pos-xs current)]
      ;; (dorun (map (fn [v]
      (run! (fn [v]
                    (apply pr/draw-blk-line ctx
                           (concat v [:size size :color color])))
     ;; (println [x y])
     ;; [x y]
      ;; (apply pr/draw-blk-line ctx
             [[x y (+ x w) y]
              [x y x (+ y h)]
              [(+ x w) (+ y h) x  (+ y h) ]
              [(+ x w) (+ y h) (+ x w) y]]))) ;; )

  ;; (update-sprite [sp]
  ;;   (let [[current length pos-xs] ((juxt :current :timeout :pos-xs) sp)
  ;;         pos-len (count pos-xs)
  ;;         tcurr (cond true (inc current) ;; add play sound
  ;;                     false (dec current);; add play sound
  ;;                     :else current)
  ;;         ncurr (cond (>= tcurr pos-len ) 0
  ;;                     (< tcurr 0) (pos-len -1)
  ;;                     :else tcurr)
  ;;         ntime (+ js/Date.now 200)]
  ;;     (assoc sp :timeout ntime :current ncurr)))

  (update-sprite [sp dir]
    (let [
          ;; [current length pos-xs] ((juxt :current :timeout :pos-xs) sp)
          {:keys [current length pos-xs]} sp
          pos-len (count pos-xs)
          tcurr (case dir
                  :up (dec current);; add play sound
                  :down (inc current) ;; add play sound
                  :else current)
          ncurr (cond (>= tcurr pos-len ) 0
                      (< tcurr 0) (dec pos-len)
                      :else tcurr)
          ntime (+ (js/Date.now) 200)]
      (assoc sp :timeout ntime :current ncurr)))
  )

(defn make-cursor [ w h pos-xs size ]
  (Cursor. w h pos-xs size "#FFF" 0 (+ (js/Date.now) 200)))
