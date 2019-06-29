(ns dativity.spec
  (:require [clojure.spec.alpha :as s]
            [ysera.test :refer [is-not]]
            [clojure.test :refer [deftest is]]
            [clojure.test.check]))

;node
(s/def ::type #{:action :data :role})

(s/def ::model-node (s/keys :req [::type]))

;edges
(s/def ::conditional-association #{:requires-conditional})

(s/def ::association #{:requires :produces :performs})

(s/def ::model-edge (s/keys :req [::association]))

(s/def ::condition (s/fspec :args (s/cat :data keyword?)))  ; map-of?

(s/def ::data-parameter keyword?)

(s/def ::conditional-model-edge (s/keys :req [::conditional-association ::condition ::data-parameter]))

(s/def ::nodes (s/map-of keyword? ::model-node))

(s/def ::edges (s/map-of (s/and vector?
                                #(= (count %) 2)
                                #(every? keyword? %))
                         ::model-edge))

(s/def ::process-model (s/keys :req [::nodes ::edges]))

(deftest specs
  (is (s/valid? ::type :role))
  (is-not (s/valid? ::type :dog))
  (is (s/valid? ::model-node {::type :role}))
  (is-not (s/valid? ::model-node {::type :bird}))
  (is (s/valid? ::conditional-model-edge {::association    :requires-conditional
                                          ::condition      (fn [data]
                                                             (> (:amount data) 300000))
                                          ::data-parameter :b
                                          :src             :i
                                          :dest            :j}))
  (is (s/explain ::process-model {::nodes {:e {::type :role}
                                           :g {::type :action}
                                           :j {::type :data}}
                                  ::edges {[:b :e] {::association :requires :src :b :dest :e}
                                           [:f :c] {::association :produces :src :f :dest :c}
                                           [:c :d] {::association :performs :src :c :dest :d}
                                           [:i :j] {::association    :requires-conditional
                                                    ::condition      (fn [data]
                                                                       (> (:amount data) 300000))
                                                    ::data-parameter :b
                                                    :src             :i
                                                    :dest            :j}}}))

  )
