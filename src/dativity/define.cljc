(ns dativity.define
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [clojure.spec.alpha :as s]
            [dativity.spec :as spec]
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

(defn action-possible-conditional
  "predicate is a function that takes one argument, the provided prereq data node"
  [action prereq predicate]
  [action prereq {:association :possible-conditional
                  :condition   predicate}])

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
  [model relationship]
  (graph/add-directed-edge model relationship))

;; below is code related to creating the model via create-model.

(defn contains-it?
  [it coll]
  (some #{it} coll))

(defn- error-when-missing
  {:test (fn []
           (is (error? (error-when-missing :a [] "error!!")))
           (is (nil? (error-when-missing :a [:a] "error!!"))))}
  [needle haystack err-msg]
  (when-not (contains-it? needle haystack)
    (error err-msg)))

(defn- validate-relationships
  [{:keys [actions
           data
           roles
           action-produces
           action-requires
           action-requires-conditional
           action-possible-conditional
           role-performs]}]
  (doseq [[action produces] action-produces]
    (let [relationship-string (str "[" action " produces " produces "]: ")]
      (error-when-missing action actions (str "Error when parsing relationship " relationship-string action " is not a defined action"))
      (error-when-missing produces data (str "Error when parsing relationship " relationship-string data " is not a defined data"))))

  (doseq [[action requires] action-requires]
    (let [relationship-string (str "[" action " requires " requires "]: ")]
      (error-when-missing action actions (str "Error when parsing relationship " relationship-string action " is not a defined action"))
      (error-when-missing requires data (str "Error when parsing relationship " relationship-string requires " is not a defined data"))))

  (doseq [[role performs] role-performs]
    (let [relationship-string (str "[" role " performs " performs "]: ")]
      (error-when-missing role roles (str "Error when parsing relationship " relationship-string role " is not a defined role"))
      (error-when-missing performs actions (str "Error when parsing relationship " relationship-string performs " is not a defined action"))))

  (doseq [{:keys [action requires condition-argument]} action-requires-conditional]
    (let [relationship-string (str "[" action " conditionally requires " requires " depending on " condition-argument "]: ")]
      (error-when-missing action actions (str "Error when parsing relationship " relationship-string action " is not a defined action"))
      (error-when-missing requires data (str "Error when parsing relationship " relationship-string requires " is not a defined data"))
      (error-when-missing condition-argument data (str "Error when parsing relationship " relationship-string condition-argument " is not a defined data"))))

  (doseq [{:keys [action condition-argument]} action-possible-conditional]
    (let [relationship-string (str "[" condition-argument " conditionally enables " action "]: ")]
      (error-when-missing action actions (str "Error when parsing relationship " relationship-string action " is not a defined action"))
      (error-when-missing condition-argument data (str "Error when parsing relationship " relationship-string condition-argument " is not a defined data"))))
  true)

(defn- validate-spec-and-rules
  [input]
  (when-not (s/valid? ::spec/model-input input)
    (error (s/explain-str ::spec/model-input input)))
  (validate-relationships input))

(defmulti add-to-model (fn [_ t _] t))
(defmethod add-to-model :actions [model _ actions]
  (reduce (fn [model action-to-add] (add-entity-to-model model (action action-to-add))) model actions))

(defmethod add-to-model :data [model _ datas]
  (reduce (fn [model data-to-add] (add-entity-to-model model (data data-to-add))) model datas))

(defmethod add-to-model :roles [model _ roles]
  (reduce (fn [model role-to-add] (add-entity-to-model model (role role-to-add))) model roles))

(defmethod add-to-model :action-produces [model _ _action-produces]
  (reduce (fn [model [action produces]] (add-relationship-to-model model (action-produces action produces))) model _action-produces))

(defmethod add-to-model :action-requires [model _ _action-requires]
  (reduce (fn [model [action requires]] (add-relationship-to-model model (action-requires action requires))) model _action-requires))

(defmethod add-to-model :action-requires-conditional [model _ _action-requires-conditional]
  (reduce (fn [model {:keys [action requires condition condition-argument]}]
            (add-relationship-to-model model (action-requires-conditional action requires condition condition-argument))) model _action-requires-conditional))

(defmethod add-to-model :action-possible-conditional [model _ _action-possible-conditional]
  (reduce (fn [model {:keys [action condition-argument condition]}]
            (add-relationship-to-model model (action-possible-conditional action condition-argument condition))) model _action-possible-conditional))

(defmethod add-to-model :role-performs [model _ _role-performs]
  (reduce (fn [model [role action]] (add-relationship-to-model model (role-performs role action))) model _role-performs))

(defn create-model
  "Creates a process model to be used by core functions.
   Takes a map with a strict structure as input"
  {:test (fn []
           (is (create-model {:actions                     [:call-mom
                                                            :call-dad
                                                            :call-grandma]

                              :data                        [:mom-number
                                                            :mom-info
                                                            :dad-info]

                              :roles                       [:me]

                              :action-produces             [[:call-mom :mom-info]
                                                            [:call-dad :dad-info]]

                              :action-requires             [[:call-mom :mom-number]
                                                            [:call-dad :mom-info]]

                              :action-requires-conditional [{:action             :call-grandma
                                                             :requires           :dad-info
                                                             :condition          (fn [mom-info]
                                                                                   (not (:grandma-number mom-info)))
                                                             :condition-argument :mom-info}]

                              :action-possible-conditional [{:action             :call-grandma
                                                             :condition-argument :dad-info
                                                             :condition          (fn [mom-info]
                                                                                   (not (:grandma-number mom-info)))}]

                              :role-performs               [[:me :call-dad]
                                                            [:me :call-mom]
                                                            [:me :call-grandma]]})))}
  [model-arguments]
  {:pre [(validate-spec-and-rules model-arguments)]}
  (reduce-kv add-to-model (empty-process-model) model-arguments))
