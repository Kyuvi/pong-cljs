(ns pong.obj
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require [pong.prep :as pr]
            [pong.blkchars :as lt]
            [pong.rfm.subs :as subs]
            [sutils.rf :as rfu])
  )

(def ball-speed 10)
(def paddle-step 10 )
(def paddle-len (* 8 pr/grid-size) )

(defprotocol SpriteProtocol
(get-pos [sp])
(draw-sprite [sp ctx])
(update-sprite [sp]
               [sp input])
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
          [p1 p2] ((juxt :p1 :p2) sp)
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
          [size top bottom] ((juxt :size :top :bottom) sp)
          ty (cond (pressed-set kup) (- y paddle-step)
                   (pressed-set kdn) (+ y paddle-step)
                   :else y)
          ;; nx (if (.-isDown kup) (- y paddle-step) )
          ;; ty (cond (= dir :up) (- y paddle-step)
          ;;          (= dir :down) (+ y paddle-step)
          ;;          :else y)
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
          ;; [kup kdn] ((juxt :key-up :key-down) sp)
          [size top bottom] ((juxt :size :top :bottom) sp)
          ;; nx (if (.-isDown kup) (- y paddle-step) )
          ;; nx (if (.-isDown kup) (- y paddle-step) )
          ty (cond (= dir :up) (- y paddle-step)
                   (= dir :down) (+ y paddle-step)
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
          new-speed (cond (and count (< count 80)) (/ ball-speed 2)
                          (and count (< count 160) (< speed ball-speed)) ball-speed
                          :else speed)
          ]
      (when edge-pred (pr/play-wall))
      (assoc sp :pos (make-spos nx ny) :speed new-speed :direction ndir
             :tick-count new-count)
      )))

(defn make-ball [x y speed direction top bottom]
  (Ball. (make-spos x y) speed direction top bottom 0))

(defrecord Cursor [w h pos-xs size color current timeout]
  SpriteProtocol
  (draw-sprite [sp ctx]
    (let [[w h size color] ((juxt :w :h :size :color) sp)
          pos-xs (:pos-xs sp)
          current (:current sp)
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
              [(+ x w) (+ y h) (+ x w) y]]
      )
      ;; )
      ))

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
    (let [[current length pos-xs] ((juxt :current :timeout :pos-xs) sp)
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
