(defproject dativity "2.0.1"
  :description "stateless, data driven process engine"
  :url "https://github.com/agentbellnorm/dativity"
  :license {:name "MIT"
            :url "https://github.com/agentbellnorm/dativity/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ysera "1.3.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [ubergraph "0.5.3"]]
  :test-paths ["test"]
  :source-paths ["src"]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild {
              :builds [{:source-paths ["src"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true}}]})
