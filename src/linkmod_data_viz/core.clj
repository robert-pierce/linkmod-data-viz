(ns linkmod-data-viz.core
    (:require [oz.core :as oz]
              [oz.server :as server]
              [cheshire.core :as json]
              [clojure.pprint :as pp]
              ;[figwheel-sidecar.repl-api :as figwheel]
              ))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

#_(defn dev! []
  (figwheel/start-figwheel!))

#_(defn do-it-fools! []
  (dev!)
  (server/start-plot-server!))

;(def browser-repl figwheel/cljs-repl)


;; Here is some example usage you can play with at the repl

  ;; Start the plot server
#_(do-it-fools!) ;; for figwheel dev

#_(oz/start-plot-server! 3000)

  ;; define a function for generating some dummy data

(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

  ;; Define a simple plot, inlining the data

(def line-plot
  {:data {:values (play-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "time"}
              :y {:field "quantity"}
              :color {:field "item" :type "nominal"}}
   :mark "line"})

  ;; Render the plot to the
 
#_(oz/view! line-plot)
#_(oz/view! [:div
           [:h1 "Hello pepe"]
           [:vega line-plot]]
          :port 3000)

  ;; We can also try publishing the plot like so (requires auth; see README.md for setup)

#_(oz/publish! line-plot)
  ;; Then follow the vega-editor link.

  ;; Build a more intricate plot
  ;; (Note here also that we're doing the Right Thing (TM) and including the field types...)

(def stacked-bar
  {:data {:values (play-data "munchkin" "witch" "dog" "lion" "tiger" "bear")}
   :mark "bar"
   :encoding {:x {:field "time"
                  :type "ordinal"}
              :y {:aggregate "sum"
                  :field "quantity"
                  :type "quantitative"}
              :color {:field "item"
                      :type "nominal"}}})

#_(oz/view! stacked-bar)


  ;; vega example

#_(def contour-plot (json/parse-string (slurp "examples/contour-lines.vega.json"))) 
#_(oz/view! contour-plot :mode :vega)

  ;; Note that to publish vega, you must set :mode

#_(oz/publish! contour-plot :mode :vega)

  ;; Construct a composite document using hiccup

(def viz
  [:div
   [:h1 "Look ye and behold"]
   [:p "A couple of small charts"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega-lite line-plot]
    [:vega-lite stacked-bar]]
   [:p "A wider, more expansive chart"]
   [:vega contour-plot]
   [:h2 "If ever, oh ever a viz there was, the vizard of oz is one because, because, because..."]
   [:p "Because of the wonderful things it does"]])

;(oz/view! viz)

  ;; And finally, we can publish this document to a github gist and load via ozviz.io

;(oz/publish! viz)

  ;; Test out live reloading functionality


;(oz/live-view! "examples/test.md")

  ;; Then edit the file at `examples/test.md` and watch

  ;; Can live reload code as well


;(oz/kill-watchers!)
;(oz/live-reload! "dev/watchtest.clj")

;:end-examples

