(ns dativity.graph
  (:require [clojure.set :refer [subset?]]
            [ysera.test :refer [is is=]]))

(defn empty-graph
  []
  {:nodes {} :edges {}})


(defn add-node-with-attrs
  {:test (fn []
           (is (-> (empty-graph)
                   (add-node-with-attrs [:node {:color "green"}])
                   (add-node-with-attrs [:node2 {:color "blue"}]))))}
  [graph [name attributes]]
  (assoc-in graph [:nodes name] attributes))

(defn add-directed-edge
  {:test (fn []
           (is= {[:node :node2] {:color "yellow"
                                 :src   :node
                                 :dest  :node2}}
                (-> (empty-graph)
                    (add-node-with-attrs [:node {:color "green"}])
                    (add-node-with-attrs [:node2 {:color "blue"}])
                    (add-directed-edge [:node :node2 {:color "yellow"}])
                    (:edges))))}
  [graph [src dst attributes]]
  (assoc-in graph [:edges [src dst]] (assoc attributes :src src :dest dst)))


(defn nodes
  {:test (fn []
           (is= #{:node :node2} (-> (empty-graph)
                                    (add-node-with-attrs [:node {:color "green"}])
                                    (add-node-with-attrs [:node2 {:color "blue"}])
                                    (add-directed-edge [:node :node2 {:color "yellow"}])
                                    (nodes)
                                    (set))))}
  [graph]
  (keys (:nodes graph)))

(defn attr
  {:test (fn []
           (is= "green" (-> (empty-graph)
                             (add-node-with-attrs [:node {:color "green"}])
                             (add-directed-edge [:node :node2 {:color "yellow"}])
                             (attr :node :color)))
           (is= "yellow" (-> (empty-graph)
                              (add-node-with-attrs [:node {:color "green"}])
                              (add-directed-edge [:node :node2 {:color "yellow"}])
                              (attr :node :node2 :color))))}
  ([graph node attr-key] (get-in graph [:nodes node attr-key]))
  ([graph src dst attr-key] (get-in graph [:edges [src dst] attr-key])))

(defn get-attr
  [node-or-edge attr]
  (get node-or-edge attr))

(defn count-nodes
  {:test (fn []
           (is= 2 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node :node2 {:color "yellow"}])
                       count-nodes)))}
  [graph]
  (count (keys (:nodes graph))))

(defn count-edges
  {:test (fn []
           (is= 1 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node :node2 {:color "yellow"}])
                       count-edges)))}
  [graph]
  (count (keys (:edges graph))))

(defn find-edges
  {:test (fn []
           (is= 1 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node :node2 {:color "yellow"
                                                         :type  :likes}])
                       (add-directed-edge [:node2 :node {:color "yellow"
                                                         :type  :hates}])
                       (find-edges {:color "yellow"
                                    :type  :hates})
                       (count)))
           (is= 2 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node :node2 {:color "yellow"
                                                         :type  :likes}])
                       (add-directed-edge [:node2 :node {:color "yellow"
                                                         :type  :hates}])
                       (find-edges {:color "yellow"})
                       (count)))
           (is= 1 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node2 :node {:color "yellow"
                                                         :type  {:flash "thunder"
                                                                 :marco "polo"}}])
                       (find-edges {:type {:flash "thunder"
                                           :marco "polo"}})
                       (count)))
           (is= 0 (-> (empty-graph)
                       (add-node-with-attrs [:node {:color "green"}])
                       (add-node-with-attrs [:node2 {:color "blue"}])
                       (add-directed-edge [:node2 :node {:color "yellow"
                                                         :type  {:flash "thunder"
                                                                 :marco "polo"}}])
                       (find-edges {:type {:flash "kartoffel"
                                           :marco "polo"}})
                       (count)))
           (is= [{:src   :node
                   :dest  :node2
                   :color "green"
                   :type  "likes"}
                  {:src   :node
                   :dest  :node3
                   :color "green"
                   :type  "meh"}]
               (-> (empty-graph)
                   (add-node-with-attrs [:node {:color "green"}])
                   (add-node-with-attrs [:node2 {:color "blue"}])
                   (add-node-with-attrs [:node3 {:color "purple"}])
                   (add-directed-edge [:node2 :node {:color "yellow"
                                                     :type  :hates}])
                   (add-directed-edge [:node :node2 {:color "green"
                                                     :type  "likes"}])
                   (add-directed-edge [:node :node3 {:color "green"
                                                     :type  "meh"}])
                   (find-edges {:src   :node
                                :color "green"}))))}
  [graph attr-query]
  (->> (:edges graph)
       (filter (fn [[_ edge-attributes]]
                 (subset? (set attr-query) (set edge-attributes))))
       (map val)))
