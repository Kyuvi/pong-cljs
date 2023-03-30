(ns pong.rfm.subs
  "Namespace containing re-frames Subscription registrations"
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require [re-frame.core :as rf])
  )


(rf/reg-sub
 ::state
 (fn [db _] (:state db)))

(rf/reg-sub
 ::previous
 (fn [db _] (:previous db)))

(rf/reg-sub
 ::mode
 (fn [db _] (get-in db [:state :mode])))

(rf/reg-sub
 ::score
 (fn [db _] (get-in db [:state :score])))

(rf/reg-sub
 ::cursor
 (fn [db _] (get-in db [:state :cursor])))

(rf/reg-sub
 ::previous-mode
 (fn [db _] (get-in db [:previous :mode])))

(rf/reg-sub
 ::previous-score
 (fn [db _] (get-in db [:previous :score])))

(rf/reg-sub
 ::current-cur
 (fn [db _] (get-in db [:state :cursor :current])))

        ;;;; game ;;;;

(rf/reg-sub
 ::paddle-one
 (fn [db _] (get-in db [:state :scene :paddle-one])))

(rf/reg-sub
 ::paddle-two
 (fn [db _] (get-in db [:state :scene :paddle-two])))

(rf/reg-sub
 ::ball
 (fn [db _] (get-in db [:state :scene :ball])))

(rf/reg-sub
 ::paused
 (fn [db _] (get-in db [:state :scene :paused])))

        ;;;; settings ;;;;

(rf/reg-sub
 ::rounds
 (fn [db _] (get-in db [:state :settings :rounds])))

(rf/reg-sub
 ::difficulty
 (fn [db _] (get-in db [:state :settings :difficulty])))

(rf/reg-sub
 ::controls
 (fn [db _] (get-in db [:state :settings :controls])))

(rf/reg-sub
 ::p-one-keys
 (fn [db _] (get-in db [:state :settings :controls :p1])))

(rf/reg-sub
 ::p-two-keys
 (fn [db _] (get-in db [:state :settings :controls :p2])))



        ;;;; key inputs ;;;;
;
(rf/reg-sub
 ::key-input
 (fn [db _] (:key-input db)))

(rf/reg-sub
 ::pressed
 (fn [db _] (get-in db [:key-input :pressed])))
