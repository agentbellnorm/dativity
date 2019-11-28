(ns dativity.spec
  (:require
    [clojure.spec.alpha :as s]))

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

(s/def ::action-possible-conditional-item (s/keys :req-un [::action
                                                           ::condition-argument
                                                           ::condition]))

(s/def ::action-requires-conditional (s/coll-of ::action-requires-conditional-item
                                                :kind vector?
                                                :distinct true))


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
