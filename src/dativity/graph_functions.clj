(ns dativity.graph-functions
  (:require [ubergraph.core :as uber]
            [clojure.test :refer :all]))

(defn empty-graph
  []
  (uber/graph))


(defn add-node-with-attrs
  {:test (fn []
           (is (-> (empty-graph)
                   (add-node-with-attrs [:node {:color "green"}])
                   (add-node-with-attrs [:node2 {:color "blue"}]))))}
  [graph node]
  (uber/add-nodes-with-attrs graph node))

(defn add-directed-edge
  {:test (fn []
           (is (= "yellow" (-> (empty-graph)
                               (add-node-with-attrs [:node {:color "green"}])
                               (add-node-with-attrs [:node2 {:color "blue"}])
                               (add-directed-edge [:node :node2 {:color "yellow"}])
                               (attr :node :node2 :color)))))}
  [graph edge]
  (uber/add-directed-edges graph edge))


(defn nodes
  {:test (fn []
           (is (= #{:node :node2} (-> (empty-graph)
                                      (add-node-with-attrs [:node {:color "green"}])
                                      (add-node-with-attrs [:node2 {:color "blue"}])
                                      (add-directed-edge [:node :node2 {:color "yellow"}])
                                      (nodes)
                                      (set)))))}
  [graph]
  (uber/nodes graph))

(defn attr
  {:test (fn []
           (is (= "green" (-> (empty-graph)
                              (add-node-with-attrs [:node {:color "green"}])
                              (add-directed-edge [:node :node2 {:color "yellow"}])
                              (attr :node :color))))
           (is (= "yellow" (-> (empty-graph)
                               (add-node-with-attrs [:node {:color "green"}])
                               (add-directed-edge [:node :node2 {:color "yellow"}])
                               (attr :node :node2 :color)))))}
  ([graph node attr-key] (uber/attr graph node attr-key))
  ([graph src dst attr-key] (uber/attr graph src dst attr-key)))

(defn count-nodes
  {:test (fn []
           (is (= 2 (-> (empty-graph)
                        (add-node-with-attrs [:node {:color "green"}])
                        (add-node-with-attrs [:node2 {:color "blue"}])
                        (add-directed-edge [:node :node2 {:color "yellow"}])
                        count-nodes))))}
  [graph]
  (uber/count-nodes graph))

(defn count-edges
  {:test (fn []
           (is (= 1 (-> (empty-graph)
                        (add-node-with-attrs [:node {:color "green"}])
                        (add-node-with-attrs [:node2 {:color "blue"}])
                        (add-directed-edge [:node :node2 {:color "yellow"}])
                        count-edges))))}
  [graph]
  (uber/count-edges graph))

(defn find-edges
  {:test (fn []
           (is (= 1 (-> (empty-graph)
                        (add-node-with-attrs [:node {:color "green"}])
                        (add-node-with-attrs [:node2 {:color "blue"}])
                        (add-directed-edge [:node :node2 {:color "yellow"
                                                          :type  :likes}])
                        (add-directed-edge [:node2 :node {:color "yellow"
                                                          :type  :hates}])
                        (find-edges {:color "yellow"
                                     :type  :hates})
                        (count))))
           (is (= 2 (-> (empty-graph)
                        (add-node-with-attrs [:node {:color "green"}])
                        (add-node-with-attrs [:node2 {:color "blue"}])
                        (add-directed-edge [:node :node2 {:color "yellow"
                                                          :type  :likes}])
                        (add-directed-edge [:node2 :node {:color "yellow"
                                                          :type  :hates}])
                        (find-edges {:color "yellow"})
                        (count)))))}
  [graph attr-map]
  (uber/find-edges graph attr-map))

(defn pprint
  [graph]
  (uber/pprint graph))

(defn show-image
  [graph]
  (uber/viz-graph graph))
