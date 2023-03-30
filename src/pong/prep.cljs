(ns pong.prep
  "Preparatory values and functions for pong"
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
 (:require [ sutils.canvas :as cvu])
  )

(def version "v0.01")

(def game-view {:fps 60 :width 804 :height 600 })
(def game-modes #{:menu :single :versus :options :controls :credits :end })
(def game-difficulty ["easy" "medium" "unfair"])

(def grid-size 12)
(def margin-size (* 3 grid-size) )
(def letter-spacing (* 4 grid-size) )

(def game-border
  (let [top (+ margin-size grid-size)
        bottom (- (:height game-view) margin-size grid-size )
        left grid-size
        right (- (:width game-view) (* grid-size 2))]
    {:top top :bottom bottom :left left :right right
     :height (- bottom top) :width (- right left)}))


        ;;;; drawing functions ;;;;

(defn fill-block
  ([ctx x y ]
   (cvu/fill-square ctx x y grid-size))
  ([ctx x y size]
   (cvu/fill-square ctx x y size))
  ([ctx x y size & {:keys [color]
                    :or { color "#FFF"}}]
   (cvu/fill-square ctx x y size color true))
  )

(defn fill-seq [ctx x y xs & {:keys [size color]
                            :or {size grid-size color "#FFF" }}]
 (dotimes [jy (count xs)]
    (dotimes [ix (count (nth xs jy))]
      (when (= (nth (nth xs jy) ix) 1)
        (fill-block ctx (+ x (* ix size)) (+ y (* jy size)) size :color color))
      )) )

(defn draw-blk-line [ctx sx sy ex ey & {:keys [size color]
                                        :or {size grid-size color "#FFF" }}]
  (let [len (Math/sqrt (+ (Math/pow (- ex sx) 2) (Math/pow (- ey sy) 2)))
        sin (/ (- ey sy) len)
        cos (/ (- ex sx) len)]
    (doseq [i (range 0 (inc len) (dec size))] ;; REVIEW: size leaves spaces with decimals
    ;; (doseq [i (range 0 (inc len) size)] ;; REVIEW: size leaves spaces on horizontal
      (fill-block ctx (+ sx (* i cos)) (+ sy (* i sin)) size :color color))))


        ;;;; game sounds ;;;;

(defn make-sound-element [sound-file-name]
  ;; (let [sound-folder  "./resources/public/audio/"]
  (let [sound-folder  "./audio/"] ;; start path from (index.)html file
    (new js/Audio (str sound-folder sound-file-name))))

(def wall-sound (make-sound-element "4391__noisecollector__pongblipf-5.wav"))
(def paddle-sound (make-sound-element "4390__noisecollector__pongblipf-4.wav"))
(def score-sound (make-sound-element
                  "333785__projectsu012__8-bit-failure-sound.wav"))
(def select-sound (make-sound-element "275896__n-audioman__coin02.wav"))

(defn sound-factory [audio start stop]
  ;; (if (.paused audio)
    (.play audio)
    (js/setTimeout (fn [] (.pause audio)
                     (set! (.-currentTime audio) start )) stop))
  ;; )

(defn play-wall [] (sound-factory wall-sound 100 100))
(defn play-paddle [] (sound-factory paddle-sound 100 100))
(defn play-score [] (sound-factory score-sound 0 200))
(defn play-select [] (sound-factory select-sound 0 200))
