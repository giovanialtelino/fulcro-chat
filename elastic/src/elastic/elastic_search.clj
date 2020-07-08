(ns elastic.elastic-search
  (:require [qbits.spandex :as s]
            [clojure.spec.alpha :as spec]
            [clojure.core.async :as async]
            [cheshire.core :as cs]))

;clojure spec to check the message format before adding to elastic
(spec/def ::message_id int?)
(spec/def ::user_id #(instance? java.util.UUID %))
(spec/def ::message string?)

(def message-format (spec/keys :req-un [::message_id ::user_id ::message]))


;quickstarting elastic below
(def loren " Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et
  dolore   magna aliqua. Sapien pellentesque habitant morbi tristique. Ac turpis egestas maecenas pharetra. Leo in vitae turpis massa sed. Habitasse platea dictumst vestibulum rhoncus. Ipsum dolor sit amet consectetur adipiscing elit ut. Placerat in egestas erat imperdiet sed euismod nisi porta. Nec nam aliquam sem et tortor consequat. Eget nunc scelerisque viverra mauris. Eu tincidunt tortor aliquam nulla. Non arcu risus quis varius quam quisque id diam. Tellus in hac habitasse platea dictumst vestibulum rhoncus est. Ac tincidunt vitae semper quis lectus nulla. Integer feugiat scelerisque varius morbi enim nunc faucibus a pellentesque. Amet nisl suscipit adipiscing bibendum est ultricies integer quis. Nam at lectus urna duis convallis. Aliquet nibh praesent tristique magna sit amet purus gravida. Vitae semper quis lectus nulla at volutpat diam. Pretium quam vulputate dignissim suspendisse in est ante in.")
(defn message-generator [n]
  {:message_id n
   :user_id    (java.util.UUID/randomUUID)
   :message    loren})

(defn message-loop []
  (let [n (into [] (take 1000 (range)))
        messages (into [] (map message-generator n))]
    messages))

(def client (s/client {:hosts ["http://127.0.0.1:9200"]}))

(defn search-word [word]
  (async/<!! (s/request-chan client {:url    "/chat"
                                     :method :get
                                     :body   {:query {:match {:message word}}}})))

(defn post-message [message]
  (let [json-message (cs/encode message)]
    (async/<!! (s/request-chan client {:url    "/chat"
                                       :method :put
                                       :body   json-message}))))

(comment
  (post-message t))

(defn post-message-quickstart
  "to be run only at test"
  []
  (let [test-messages (message-loop)]
    (map post-message test-messages)))

;better to take a look at the new high level interop