(ns dativity.visualize
  (:require
    [ubergraph.core :as uber]
    [ysera.error :refer [error]]
    [ysera.test :refer [is=]]))

(defn- default-transform-node
  {:test (fn []
           (is= (default-transform-node {:type :data})
                {:type  :data
                 :color :green}))}
  [node]
  (assoc node :color (condp = (:type node)
                       :action :blue
                       :data :green
                       :role :orange
                       (error (format "could not add visuals to %s" node)))))

(defn- default-transform-edge
  {:test (fn []
           (is= (default-transform-edge {:association :produces})
                {:association :produces
                 :color       :green
                 :label       "produces"}))}
  [edge]
  (merge edge (condp = (:association edge)
                :produces {:color :green
                           :label "produces"}
                :requires {:color :red
                           :label "requires"}
                :requires-conditional {:color :purple
                                       :label "requires?"}
                :performs {:color :orange
                           :label "does"}
                (error (format "could not add visuals to %s" edge)))))

(defn- to-uber
  [graph transform-node transform-edge]
  (let [nodes (->> (:nodes graph)
                   (map (fn [[k v]] [k (transform-node v)]))
                   (vec))
        edges (->> (:edges graph)
                   (map (fn [[[src dest] v]] [src dest (transform-edge v)]))
                   (vec))]
    (uber/edn->ubergraph {:nodes            nodes
                          :directed-edges   edges
                          :allow-parallel?  false
                          :undirected?      false
                          :undirected-edges []})))

(defn visualize
  "Visualize the dativity graph

  options is a map off attributes for the graph can be (from the Ubergraph doc):
    :auto-label true (labels each node/edge with its attribute map)
    :layout :dot, :neato, :fdp, :sfdp, :twopi, or :circo
    :save {:filename _, :format _} where format is one of
    :bmp :dot :eps :gif :ico :jpg :jpeg :pdf :png :ps :ps2 :svgz :tif :tiff :vmlz :wbmp

  options can also be graph-viz attributes (see link below)

  transform-node and transform-graph takes a node or an edge and return it with graph-viz attributes.

  See graph-viz docs for details: https://graphviz.gitlab.io/_pages/doc/info/attrs.html"
  ([graph options]
   (visualize graph options default-transform-node default-transform-edge))
  ([graph options transform-node transform-edge]
   (uber/viz-graph (to-uber graph transform-node transform-edge) options)))

;;layouts that make sense are :fdp and :dot
(defn generate-png
  "requires graphviz"
  [graph]
  (visualize graph {:layout :dot}))
