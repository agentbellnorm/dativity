(ns dativity.define
  (:require [ubergraph.core :as uber]
            [clojure.test :refer :all]))

(defn empty-case-model
  []
  (uber/multigraph))

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
  [node creates]
  [node creates {:association :produces
                 :color       :green
                 :label       "produces"}])

(defn action-requires
  [node prereq]
  [node prereq {:association :requires
                :color       :red
                :label       "requires"}])

(defn role-performs
  [role action]
  [role action {:association :performs
                :color       :orange
                :label       "does"}])

(defn generate-graph-image!
  [graph name-with-path]
  (uber/viz-graph graph {:layout :fdp
                         :save   {:filename name-with-path :format :png}}))


(defn add-entity-to-model
  {:test (fn []
           (is (= 2
                  (-> (empty-case-model)
                      (add-entity-to-model (action :add-customer-information))
                      (add-entity-to-model (action :add-phone-number))
                      (uber/count-nodes)))))}
  [case node]
  (uber/add-nodes-with-attrs case node))

(defn add-relationship-to-model
  {:test (fn []
           (let [graph (-> (empty-case-model)
                           (add-entity-to-model (action :thing-to-do))
                           (add-entity-to-model (data :thing-to-know))
                           (add-relationship-to-model (action-produces :thing-to-do :thing-to-know)))]
             (is (= 1 (uber/count-edges graph)))
             (is (= 2 (uber/count-nodes graph)))))}
   [case relationship]
  (uber/add-directed-edges case relationship))


(comment (uber/pprint case-graph))
(comment (uber/ubergraph->edn case-graph))

