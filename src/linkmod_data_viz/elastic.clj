(ns linkmod-data-viz.elastic
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

(defprotocol Queryable
  (query [this index q]
    "Takes an index name and clojure map representing ElasticSearch Query DSL.
     Returns a map representing the response body payload.
     Note that the query map may use keyword or string or symbol keys (all will be normalized to json with string keys),
     and the response will use keyword keys.")
  (multiquery [this index qs]
    "Execute multiple queries against the same index.
    `qs` is a map of queries (keyed however you like).
    Returns a map of responses (keyed the same way)."))

(defn- http-json-req
  "Make a HTTP request (with an optional string body). On success, we assume the response is JSON and return the parsed response. On failure returns a maybe-style error wrapping the raw response."
  [method url body content-type opts]
  (let [resp @(method url
                      (merge
                       {:body       body
                        :headers    {"Content-Type" content-type}
                        ;; Note that httpkit defaults :keepalive to 120s.
                        ;; Duplicating here for explicitness and potential override.
                        :keepalive  120000}
                       opts))]
    (let [parsed (-> resp
                     :body
                     (json/parse-string true))]
      (if (and (#{200 201} (:status resp))
               (not (:error parsed)))
        parsed
        {:error resp}))))

(defn- build-opts
  [{:keys [user password secure?]}]
  (cond-> {}
    (not secure?) (assoc :insecure? true)
    (and user password) (assoc :basic-auth [user password])))

(defn- ndjson
  "Return newline-delimited JSON payload"
  [xs]
  ;; Empty string at end is to ensure a newline after the last item.
  (clojure.string/join "\n" (conj (mapv json/generate-string xs) "")))

(defrecord ElasticClient [url user password secure?]
  Queryable
  (query [this index q]
    "Return response body for a successful response.
     Throws an exception for a failed response."
    (let [search-url (format "%s/%s/_search" url index)
          body (json/generate-string q)
          content-type "application/json"
          opts (build-opts this)]
      (http-json-req http/post search-url body content-type opts)))
  (multiquery [this index qs]
    "Return map of results for a successful response.
     Throws an exception for a failed response."
    (let [search-url (format "%s/%s/_msearch" url (name index))
          body (let [header {:index index}]
                 (ndjson (concat [header]
                                 (interpose header (vals qs)))))
          content-type "application/x-ndjson"
          opts (build-opts this)]
      (http-json-req http/post search-url body content-type opts))))

(defn build-elastic-client
  [url user password]
  (map->ElasticClient {:url url :user user :password password :secure? false}))

(defn extract-geo-data
  "Get lat/lon from an elastic query result. Expecting only one result"
  [query-result]
  (get-in query-result [:hits :hits 0 :_source]))

(defn build-geo-query
  [rentcom-geo-url]
  {:_source ["latitude" "longitude" "lc_rentcom_geo_url"]
   :query {:bool {:filter [{:term {:lc_rentcom_geo_url rentcom-geo-url}}]}}})
