(ns pong.prep
  "Preparatory values and functions for pong"
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
 (:require [ sutils.canvas :as cvu])
  )

        ;;;; game constant values ;;;;

(def version "v0.01")

(def game-view "Values of game attributes." {:fps 60 :width 804 :height 600 })
(def game-modes #{:menu :single :versus :options :controls :credits :end })
(def game-difficulty "Vector of game difficulty." ["easy" "medium" "unfair"])

(def grid-size 12)
(def letter-spacing (* 4 grid-size) )
(def margin-size (* 3 grid-size) )

(def game-border
  (let [top (+ margin-size grid-size)
        bottom (- (:height game-view) margin-size grid-size )
        left grid-size
        right (- (:width game-view) (* grid-size 2))]
    {:top top :bottom bottom :left left :right right
     :height (- bottom top) :width (- right left)}))

(def cursor-vals "Values of cursor attributes."
  {:width (* grid-size 20) :controls-width (* grid-size 30)
   :height (* grid-size 4) :thickness 5})

(def cursor-xpos "Map of x positions for cursor."
  {:centered (/ (- (:width game-view) (:width cursor-vals)
                   (* (:thickness cursor-vals) 2))
                2)
   :left 200 })

(defn center-cursor [mode]
  (let [width-key (if (= mode :controls) :controls-width :width)]
    (/ (- (:width game-view) (width-key cursor-vals)) 2)))

(def cursor-ypos "Map of y positons vectors for relevant modes."
  {:menu [20.5 25.5 30.5 35.5]
   :options [20.5 25.5 30.5 35.5]; [28.5 33.5 38.5]
   :controls [15.5 20.5 25.5 30.5 35.5]
   :end [28.5 33.5]})


(def paddle-vals "Values of paddle attributes." {:len (* 8 grid-size) :step 10})

(def ball-speed 10)

        ;;;; game drawing functions ;;;;

(defn fill-block
  "Fill a square of size `size` on canvas with context `ctx` starting at
  position `x` and `y`. Optionally with `color` given by keyword.
  `size` and `color` default to grid-size and #FFF respectively."
  ([ctx x y ]
   (cvu/fill-square ctx x y grid-size))
  ([ctx x y size]
   (cvu/fill-square ctx x y size))
  ([ctx x y size & {:keys [color]
                    :or { color "#FFF"}}]
   (cvu/fill-square ctx x y size color true))
  )

(defn fill-seq
  "Fill blocks on canvas with context `ctx` at positions given by '1' in
  sequence(vector of vectors) `xs` starting at postitions `x`` and `y`.
  Optionally with `size` and `color` given by keywords, which default to
  grid-size and #FFF respectively."
  [ctx x y xs & {:keys [size color]
                 :or {size grid-size color "#FFF" }}]
  (dotimes [jy (count xs)]
    (dotimes [ix (count (nth xs jy))]
      (when (= (nth (nth xs jy) ix) 1)
        (fill-block ctx (+ x (* ix size)) (+ y (* jy size)) size :color color)))))

(defn draw-blk-line
  "Draw line in block form on canvas with context `ctx` from position
  `sx` and `sy` to postition `ex` and `ey`. Optionally with `size` and `color`
  given by respective keywords, which default to grid-size and #FFF respectively."
  [ctx sx sy ex ey & {:keys [size color]
                      :or {size grid-size color "#FFF" }}]
  (let [len (Math/sqrt (+ (Math/pow (- ex sx) 2) (Math/pow (- ey sy) 2)))
        sin (/ (- ey sy) len)
        cos (/ (- ex sx) len)]
    (doseq [i (range 0 (inc len) (dec size))] ;; REVIEW: size leaves spaces with decimals
    ;; (doseq [i (range 0 (inc len) size)] ;; REVIEW: size leaves spaces on horizontal
      (fill-block ctx (+ sx (* i cos)) (+ sy (* i sin)) size :color color))))


        ;;;; game sounds ;;;;

(defn make-audio-element
  "Make an new audio element(object) from file of filename `sound-file-name`
   in folder ./audio/ folder located in same folder as this projects html file."
  [sound-file-name]
  ;; (let [sound-folder  "./resources/public/audio/"]
  (let [sound-folder  "./audio/"] ;; start path from (index.)html file
    (new js/Audio (str sound-folder sound-file-name))))

(def wall-sound (make-audio-element "4391__noisecollector__pongblipf-5.wav"))
(def paddle-sound (make-audio-element "4390__noisecollector__pongblipf-4.wav"))
(def score-sound (make-audio-element
                  "333785__projectsu012__8-bit-failure-sound.wav"))
(def select-sound (make-audio-element "275896__n-audioman__coin02.wav"))

(defn sound-factory
  "Play from `start` milliseconds to `stop` miliseconds of an `audio` element."
  [audio start stop]
  ;; (if (.paused audio)
    (.play audio)
    (js/setTimeout (fn [] (.pause audio)
                     (set! (.-currentTime audio) start )) stop))
  ;; )

(defn play-wall [] (sound-factory wall-sound 100 100))
(defn play-paddle [] (sound-factory paddle-sound 100 100))
(defn play-score [] (sound-factory score-sound 0 200))
(defn play-select [] (sound-factory select-sound 0 200))
