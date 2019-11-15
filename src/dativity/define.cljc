(ns dativity.define
  (:require [ysera.test :refer [is= is is-not error?]]
            [ysera.error :refer [error]]
            [clojure.spec.alpha :as s]
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

  (doseq [{:keys [action requires]} action-possible-conditional]
    (let [relationship-string (str "[" requires " conditionally enables " action "]: ")]
      (error-when-missing action actions (str "Error when parsing relationship " relationship-string action " is not a defined action"))
      (error-when-missing requires data (str "Error when parsing relationship " relationship-string requires " is not a defined data"))))
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

(s/def ::action-possible-conditional-item (s/keys :req-un [::action
                                                           ::requires
                                                           ::condition]))

(s/def ::action-possible-conditional (s/coll-of ::action-possible-conditional-item
                                                :kind vector?
                                                :distinct true))

(s/def ::model-input (s/keys :req-un [::actions
                                      ::data
                                      ::roles
                                      ::action-produces
                                      ::action-requires
                                      ::action-requires-conditional
                                      ::action-possible-conditional
                                      ::role-performs]))

(defn- validate-spec-and-rules
  [input]
  (when-not (s/valid? ::model-input input)
    (error (s/explain-str ::model-input input)))
  (validate-relationships input))

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

                              :role-performs               [[:me :call-dad]
                                                            [:me :call-mom]
                                                            [:me :call-grandma]]})))}
  [arg-map]
  {:pre [(validate-spec-and-rules arg-map)]}
  (let [actions-arg (:actions arg-map)
        data-arg (:data arg-map)
        roles-arg (:roles arg-map)
        action-produces-arg (:action-produces arg-map)
        action-requires-arg (:action-requires arg-map)
        action-requires-conditional-arg (:action-requires-conditional arg-map)
        role-performs-arg (:role-performs arg-map)]
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
