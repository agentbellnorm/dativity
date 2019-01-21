(ns dativity.graph-functions
  (:require [ubergraph.core :as uber]))

(defn empty-graph
  []
  (uber/graph))

(defn count-nodes
  [graph]
  (uber/count-nodes graph))

(defn count-edges
  [graph]
  (uber/count-edges graph))


(defn find-edges
  [graph attr-map]
  (uber/find-edges graph attr-map))

(defn add-node-with-attrs
  [graph node]
  (uber/add-nodes-with-attrs graph node))

(defn add-directed-edge
  [graph edge]
  (uber/add-directed-edges graph edge))

(defn pprint
  [graph]
  (uber/pprint graph))

(defn show-image
  [graph]
  (uber/viz-graph graph))

(defn nodes
  [graph]
  (uber/nodes graph))

(defn attr
  [graph node attr-key]
  (uber/attr graph node attr-key))
