(ns pong.core
  {:author "Kyuvi"
   :license {:name "GPL-3.0 WITH Classpath-exception-2.0"
             :url "https://www.gnu.org/licenses/gpl-3.0.html"}}
  (:require
   [reagent.core :as rg :refer [atom]]
   [reagent.dom :as rd]
   [re-frame.core :as rf]
   [sutils.browser :as br]
   [sutils.rf :as rfu]

   [pong.blkchars :as lt]
   [pong.prep :as pr]
   [pong.kone :as kn]
   [pong.view :as vw]

   [pong.rfm.events :as events]
   [pong.rfm.subs :as subs]
   ))

(enable-console-print!)

(println "Here at src/pong/core.cljs.")

;; define your app data so that it doesn't get over-written on reload

;; (defonce app-state (atom {:text "Hello world!"}))
;;

(defn foot-notes []
  ;; [:div {:class "footer"}
  [:div.footer
  ;; [:div {:style {:position "relative" :width "100%"
  ;;                :bottom 0 :left 0 :height 150}}
  ;; <h4> "tesnt"</h4>
   [:h4 "Copyright© 2023 " [:a {:href "https://codeberg.org/Kyuvi" } "Kyuvi"]
    ]
   [:p
    "This software is released under the "
    [:a {:href "https://www.gnu.org/licenses/gpl-3.0.en.html"} "GNU GPL3"]
    " license"
    ]
   ;; [:span {:style "font-size 2em"}]
   ]
  )

(defn game-canv []
  [:div#game-frame
    ;; [vw/pcanv]
   [vw/pcanv-outer]
  ]
  )

(defn full-page []
  [:div.container
  ;; [:div {:style {
  ;;                :position "relative"
  ;;                :min-height "100%"
  ;;                :text-align "center"
  ;;                }}
   [:title "PONG [CLJS]"]
   [game-canv]
   [foot-notes] ;; REVIEW: doesn't render if no game-canv?
   ])

;; (defn render-page []
;; (rd/render ;[hello-world]
;;            ;; [foot-notes]
;;            [full-page]
;;            (. js/document (getElementById "app")))
;; )

(defn ^:dev/after-load render-page []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rd/unmount-component-at-node root-el)
    (rd/render [full-page] root-el)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn dispatch-key-handler
  "Dispatch a key event."
  [e kw]
  (let [player-keys (rfu/<sub [::subs/controls])
        [p1 p2] ((juxt :p1 :p2) player-keys)
        key-vec (apply concat (map (juxt :up :down) [p1 p2]))
        menu-keys #{"Enter" "ArrowUp" "ArrowDown" "Space" "Escape"}
        key-set (into menu-keys key-vec)
        ]
  (when (key-set e.key) (.preventDefault e))
  (println e.key)
  ;; (if-not e.repeat (rf/dispatch [kw {:key e.key :shift e.shiftKey :alt e.altKey}])))
  (rf/dispatch [kw {:event e :key e.key :shift e.shiftKey :alt e.altKey}]))
  ;; (when-not e.repeat (rf/dispatch [::events/key-up])))
)

(defonce tick (js/setInterval #(rf/dispatch [::events/tick])
                              (/ 1000 (:fps pr/game-view))))

(defn run-pong []
  (rf/dispatch-sync [::events/initialize])
  (render-page)
  ;; (tick)
  (br/add-listener js/document "keydown" #(dispatch-key-handler % ::events/key-down))
  ;; (br/add-listener js/document "keydown" #(dispatch-key-handler % :key-down))
  (br/add-listener js/document "keyup" #(dispatch-key-handler % ::events/key-up))
  ;; (.addEventListener js/document "keydown" #(rf/dispatch [::events/key-up]) )
  )

(run-pong)
