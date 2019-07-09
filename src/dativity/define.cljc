(ns dativity.define
  (:require [ysera.test :refer [is=]]
            [ysera.error :refer [error]]
            [clojure.spec.alpha :as s]
            [dativity.graph :as graph]))

(defn contains-it?
  [it coll]
  (some #{it} coll))

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

;; below is code related to creating the model via create-model.

(defn- validate-relationships
  [{:keys [actions data roles action-produces action-requires action-requires-conditional role-performs]}]
  (doseq [[action produces] action-produces]
    (let [relationship-string (str "[" action " produces " produces "]: ")]
      (when-not (contains-it? action actions)
        (error (str "Error when parsing relationship " relationship-string action " is not a defined action")))
      (when-not (contains-it? produces data)
        (error (str "Error when parsing relationship " relationship-string data " is not a defined data")))))

  (doseq [[action requires] action-requires]
    (let [relationship-string (str "[" action " requires " requires "]: ")]
      (when-not (contains-it? action actions)
        (error (str "Error when parsing relationship " relationship-string action " is not a defined action")))
      (when-not (contains-it? requires data)
        (error (str "Error when parsing relationship " relationship-string requires " is not a defined data")))))

  (doseq [[role performs] role-performs]
    (let [relationship-string (str "[" role " performs " performs "]: ")]
      (when-not (contains-it? role roles)
        (error (str "Error when parsing relationship " relationship-string role " is not a defined role")))
      (when-not (contains-it? performs actions)
        (error (str "Error when parsing relationship " relationship-string performs " is not a defined action")))))

  (doseq [{:keys [action requires condition-argument]} action-requires-conditional]
    (let [relationship-string (str "[" action " conditionally requires " requires " depending on " condition-argument "]: ")]
      (when-not (contains-it? action actions)
        (error "Error when parsing relationship " relationship-string action " is not a defined action"))
      (when-not (contains-it? requires data)
        (error (str "Error when parsing relationship " relationship-string requires " is not a defined data")))
      (when-not (contains-it? condition-argument data)
        (error (str "Error when parsing relationship " relationship-string condition-argument " is not a defined data")))))
  true)

(s/def ::relationship (s/coll-of keyword? :kind vector? :count 2))

(s/def ::actions (s/coll-of keyword? :kind vector?))
(s/def ::data (s/coll-of keyword? :kind vector?))
(s/def ::roles (s/coll-of keyword? :kind vector?))
(s/def ::action-produces (s/coll-of ::relationship :kind vector?))
(s/def ::action-requires (s/coll-of ::relationship :kind vector?))
(s/def ::role-performs (s/coll-of ::relationship :kind vector?))

(s/def ::action keyword?)
(s/def ::requires keyword?)
(s/def ::condition fn?)
(s/def ::condition-argument keyword?)
(s/def ::action-requires-conditional-item (s/keys :req-un [::action
                                                           ::requires
                                                           ::condition
                                                           ::condition-argument]))

(s/def ::action-requires-conditional (s/coll-of ::action-requires-conditional-item
                                                :kind vector?
                                                :distinct true))

(s/def ::model-input (s/keys :req-un [::actions
                                      ::data
                                      ::roles
                                      ::action-produces
                                      ::action-requires
                                      ::action-requires-conditional
                                      ::role-performs]))

(defn- validate-spec-and-rules
  [input]
  (when-not (s/valid? ::model-input input)
    (error (s/explain-str ::model-input input)))
  (validate-relationships input))

(defn create-model
  "Creates a process model to be used by core functions.
   Takes a map with a strict structure as input, check specs or tests"
  [input]
  {:pre [(validate-spec-and-rules input)]}
  (let [actions-arg (:actions input)
        data-arg (:data input)
        roles-arg (:roles input)
        action-produces-arg (:action-produces input)
        action-requires-arg (:action-requires input)
        action-requires-conditional-arg (:action-requires-conditional input)
        role-performs-arg (:role-performs input)]
    (as-> (empty-process-model) model
          (reduce (fn [acc input-action]
                    (add-entity-to-model acc (action input-action))) model actions-arg)
          (reduce (fn [acc input-data]
                    (add-entity-to-model acc (data input-data))) model data-arg)
          (reduce (fn [acc input-role]
                    (add-entity-to-model acc (role input-role))) model roles-arg)
          (reduce (fn [acc [action produces]]
                    (add-relationship-to-model acc (action-produces action produces))) model action-produces-arg)
          (reduce (fn [acc [action requires]]
                    (add-relationship-to-model acc (action-requires action requires))) model action-requires-arg)
          (reduce (fn [acc [role performs]]
                    (add-relationship-to-model acc (role-performs role performs))) model role-performs-arg)
          (reduce (fn [acc {:keys [action requires condition condition-argument]}]
                    (add-relationship-to-model acc (action-requires-conditional action requires condition condition-argument))) model action-requires-conditional-arg))))
