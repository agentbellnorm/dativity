(ns define
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [is]])
                    [graph :as graph]))

(defn empty-case-model
  []
  (graph/empty-graph))

(defn action
  [name]
  [name {:type  :action
         :color :blue}])

(defn data
  [name]
  [name {:type  :data
         :color :green}])

(defn role
  [name]
  [name {:type  :role
         :color :orange}])

(defn action-produces
  [action creates]
  [action creates {:association :produces
                   :color       :green
                   :label       "produces"}])

(defn action-requires
  [action prereq]
  [action prereq {:association :requires
                  :color       :red
                  :label       "requires"}])

(defn action-requires-conditional
  "condition fn can assume that the data exists"
  [action prereq predicate data-parameter]
  [action prereq {:association    :requires-conditional
                  :color          :purple
                  :label          "requires?"
                  :condition      predicate
                  :data-parameter data-parameter}])

(defn role-performs
  [role action]
  [role action {:association :performs
                :color       :orange
                :label       "does"}])

(defn add-entity-to-model
  {:test (fn []
           (is (= 2
                  (-> (empty-case-model)
                      (add-entity-to-model (action :add-customer-information))
                      (add-entity-to-model (action :add-phone-number))
                      (graph/count-nodes)))))}
  [case node]
  (graph/add-node-with-attrs case node))

(defn add-relationship-to-model
  {:test (fn []
           (let [graph (-> (empty-case-model)
                           (add-entity-to-model (action :thing-to-do))
                           (add-entity-to-model (data :thing-to-know))
                           (add-relationship-to-model (action-produces :thing-to-do :thing-to-know)))]
             (is (= 1 (graph/count-edges graph)))
             (is (= 2 (graph/count-nodes graph)))))}
  [case relationship]
  (graph/add-directed-edge case relationship))
