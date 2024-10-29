(ns probe
  (:require [org.httpkit.client :as hk-client]
            [cheshire.core :as json]))

(def otel-endpoint (or (System/getenv "OTEL_ENDPOINT")
                       "http://localhost:9090/api/v1/otlp/v1/metrics"))

(def latest-block-request-body
  (json/generate-string {:jsonrpc "2.0"
                         :method "eth_getBlockByNumber"
                         :params ["latest",false]
                         :id 42}))

(defn eth-get-lates-block-request []
  (hk-client/request {:url "https://ethereum.publicnode.com" :method :post
                      :headers {"Content-Type" "application/json"}
                      :body latest-block-request-body
                      :timeout 2000}))

(defn hex-to-int [hex-str]
  (Integer/parseInt (subs hex-str 2) 16))

(defn eth-get-latest-block []
  (let [response @(eth-get-lates-block-request)
        status (:status response)]
    (if (not= status 200)
      (throw (Exception. (str "Failed to get latest block. response: " response)))
      (-> response
          :body
          (json/parse-string)
          (get-in ["result" "number"])
          (hex-to-int)))))

(defn unix-time-nanos []
  (* (System/currentTimeMillis) 1000000))

(defn metric-body
  "in simple app like this exporter we can send metric data as pure http request.
   In more complex applications I'm going to use library like https://github.com/steffan-westcott/clj-otel"
  [value timestamp]
  (json/generate-string
   {:resourceMetrics
    [{:resource
      {:attributes
       [{:key "service.name"
         :value {:stringValue "sv-scripting-assesment"}}]}
      :scopeMetrics
      [{:metrics
        [{:name "eth.latest.block"
          :unit "number"
          :description "latest ETH block"
          :gauge
          {:dataPoints
           [{:asDouble value
             :timeUnixNano timestamp
             :attributes
             [{:key "source"
               :value {:stringValue "babashka"}}]}]}}]}]}]}))


(defn send-metric-request [endpoint body]
  (hk-client/request {:url endpoint :method :post
                      :headers {"Content-Type" "application/json"}
                      :body body
                      :timeout 2000}))

(defn send-metric
  "send metrics to OTLP endpoint"
  [value]
  (let [response @(send-metric-request otel-endpoint (metric-body value (str (unix-time-nanos))))]
    (if (not= (:status response) 200)
      (throw (Exception. (str "Failed to send metric. Status: " (:status response))))
      (println "Metric sent. Value: " value))))

(defn -main []
  (loop [exec-time (System/currentTimeMillis)]
    (Thread/sleep (- exec-time (System/currentTimeMillis)))
    (let [latest-block (eth-get-latest-block)]
      (println "Latest block: " latest-block)
      (send-metric latest-block)
      (recur (+ exec-time 5000)))))

(-main)
