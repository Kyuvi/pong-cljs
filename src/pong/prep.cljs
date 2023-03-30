(ns pong.prep
  "Preparatory values and functions for pong"
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
 (:require [ sutils.canvas :as cvu])
  )

(def grid-size 12)
(def margin-size (* 3 grid-size) )
(def letter-spacing (* 4 grid-size) )


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
