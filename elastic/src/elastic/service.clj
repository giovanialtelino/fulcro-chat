(ns elastic.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [cheshire.core :as cs]
            [elastic.elastic-search :as el]
            [ring.util.response :as ring-resp]
            [clojure.core.async :as async]))

;docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.8.0

(defn- ring-response-from-body-to-json [response]
  (println "repz" response)
  (ring-resp/content-type
    (ring-resp/response (cs/encode (:body response)))
    "application/json"))

(defn search-word
  [{{:keys [word]} :path-params}]
  (let [c (async/chan)]
    (async/go
      (async/>! c (el/search-word word))
      (async/close! c))
    (ring-response-from-body-to-json (async/<!! c))))

(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/search-word/:word" :get (conj common-interceptors `search-word) :route-name :search-word]})

(def service {:env                     :prod
              ::http/routes            routes
              ::http/resource-path     "/public"
              ::http/type              :jetty
              ::http/port              8080
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        :ssl? false
                                        }})
