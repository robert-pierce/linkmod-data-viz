 (ns linkmod-data-viz.linkmod-viz
  (:require [oz.core :as oz]
            [org.httpkit.client :as http]
            [oz.server :as server]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

(def line-plot
  {:data {:values (play-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "time"}
              :y {:field "quantity"}
              :color {:field "item" :type "nominal"}}
   :mark "line"})

(defn read-csv
  [url]
  (let [data (with-open [r (io/reader url)]
               (doall 
                (csv/read-csv r)))]
    (map zipmap
         (->> (first data)
              (map keyword)
              repeat)
         (rest data))))

(def topo-viz
  {:$schema "https://vega.github.io/schema/vega-lite/v3.json"
   :width 900
   :height 560
   :projection {:type "albersUsa"}
   :layer [{:data {:name "states"
                   :url "https://vega.github.io/vega-tutorials/airports/data/us-10m.json"
                   :format {:type "topojson"
                      :feature "states"}}
            :mark {:type "geoshape"
                   :fill "lightgray"
                   :stroke "white"}}
           {:data {:url "https://gist.github.com/robert-pierce/cc4ba2da18aaaf81cccbb9323e34ccaa"}}

           ]})
