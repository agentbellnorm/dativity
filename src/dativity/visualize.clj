(ns dativity.visualize
  (:require
    [ubergraph.core :as uber]))

(defn- to-uber
  [graph]
  (let [nodes (->> (:nodes graph)
                   (map (fn [[k v]] [k v]))
                   (vec))
        edges (->> (:edges graph)
                   (map (fn [[[src dest] v]] [src dest v]))
                   (vec))]
    (uber/edn->ubergraph {:nodes            nodes
                          :directed-edges   edges
                          :allow-parallel?  false
                          :undirected?      false
                          :undirected-edges []})))

;layouts that make sense are :fdp and :dot
(defn generate-png
  "requires graphviz"
  [graph]
  (uber/viz-graph (to-uber graph) {:layout :dot}))
