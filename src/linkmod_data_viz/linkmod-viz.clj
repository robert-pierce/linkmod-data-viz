 (ns linkmod-data-viz.linkmod-viz
  (:require [oz.core :as oz]
            [org.httpkit.client :as http]
            [oz.server :as server]
            [clojure.java.io :as io]
            [linkmod-data-viz.Srp-Apt-Top-300 :as Srp-Apt-Vega]
            [clojure.data.csv :as csv]))

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
