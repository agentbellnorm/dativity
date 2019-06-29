(ns dativity.define
  (:require [ysera.test :refer [is=]]
            [dativity.graph :as graph]))

(defn empty-process-model
  []
  (graph/empty-graph))

(defn action
  [name]
  [name {:type :action}])

(defn data
  [name]
  [name {:type :data}])

(defn role
  [name]
  [name {:type :role}])

(defn action-produces
  [action creates]
  [action creates {:association :produces}])

(defn action-requires
  [action prereq]
  [action prereq {:association :requires}])

(defn action-requires-conditional
  "condition fn can assume that the data exists"
  [action prereq predicate data-parameter]
  [action prereq {:association    :requires-conditional
                  :condition      predicate
                  :data-parameter data-parameter}])

(defn role-performs
  [role action]
  [role action {:association :performs}])

(defn add-entity-to-model
  {:test (fn []
           (is= 2 (-> (empty-process-model)
                      (add-entity-to-model (action :add-customer-information))
                      (add-entity-to-model (action :add-phone-number))
                      (graph/count-nodes))))}
  [model node]
  (graph/add-node-with-attrs model node))

(defn add-relationship-to-model
  {:test (fn []
           (let [graph (-> (empty-process-model)
                           (add-entity-to-model (action :thing-to-do))
                           (add-entity-to-model (data :thing-to-know))
                           (add-relationship-to-model (action-produces :thing-to-do :thing-to-know)))]
             (is= 1 (graph/count-edges graph))
             (is= 2 (graph/count-nodes graph))))}
  [case relationship]
  (graph/add-directed-edge case relationship))
