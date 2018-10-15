(ns dativity.define
  (:require [ubergraph.core :as uber]
            [clojure.test :refer :all]))

(defn create-empty-case
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

(defn production-relationship
  [node creates]
  [node creates {:association :produces
                 :color       :green
                 :label       "produces"}])

(defn action-prerequisite
  [node prereq]
  [node prereq {:association :requires
                :color       :red
                :label       "requires"}])

(defn role-can-perform
  [role action]
  [role action {:association :performs
                :color       :orange
                :label       "does"}])

(defn generate-graph-image!
  [graph name-with-path]
  (uber/viz-graph graph {:layout :fdp
                         :save   {:filename name-with-path :format :png}}))


(defn add-node-to-case
  {:test (fn []
           (is (= 2
                  (-> (create-empty-case)
                      (add-node-to-case (action :add-customer-information))
                      (add-node-to-case (action :add-phone-number))
                      (uber/count-nodes)))))}
  [case node]
  (uber/add-nodes-with-attrs case node))

(defn add-relationship-to-case
  {:test (fn []
           (let [graph (-> (create-empty-case)
                           (add-node-to-case (action :thing-to-do))
                           (add-node-to-case (data :thing-to-know))
                           (add-relationship-to-case (production-relationship :thing-to-do :thing-to-know)))]
             (is (= 1 (uber/count-edges graph)))
             (is (= 2 (uber/count-nodes graph)))))}
   [case relationship]
  (uber/add-directed-edges case relationship))


(comment (uber/pprint case-graph))
(comment (uber/ubergraph->edn case-graph))

