(ns dativity.process_definition
  (:require [ubergraph.core :as uber]))

(defn init-action-node
  [name]
  [name {:type :action
         :color :blue}])

(defn init-data-node
  [name]
  [name {:type :data
         :color :green}])

(defn init-role-node
  [name]
  [name {:type :role
         :color :orange}])

(defn production-relationship
  [node creates]
  [node creates {:association :produces
                 :color :green
                 :label "produces"}])

(defn action-prerequisite
  [node prereq]
  [node prereq {:association :requires
                :color :red
                :label "requires"}])

(defn role-can-perform
  [role action]
  [role action {:association :performs
                :color :orange
                :label "does"}])

(def case-graph
  (-> (uber/multigraph
        ; Actions
        (init-action-node :create-case)
        (init-action-node :consent-to-IR)
        (init-action-node :fetch-supplimentary-info)
        (init-action-node :know-your-customer)
        (init-action-node :add-economy)
        (init-action-node :get-currently-owned-real-estate)
        (init-action-node :add-loan-details)
        (init-action-node :add-collateral)
        (init-action-node :create-collateral-link)
        (init-action-node :calculate-amortization)
        (init-action-node :produce-credit-application-document)
        (init-action-node :sign-credit-application-document)
        ; Data entities
        (init-data-node :case-id)
        (init-data-node :customer-id)
        (init-data-node :customer-info)
        (init-data-node :consent)
        (init-data-node :know-your-customer-data)
        (init-data-node :economy)
        (init-data-node :currently-owned-real-estate)
        (init-data-node :loan-details)
        (init-data-node :collateral)
        (init-data-node :collateral-link)
        (init-data-node :amortization)
        (init-data-node :credit-application-reference)
        (init-data-node :credit-application-signature)
        ; Roles
        (init-role-node :applicant)
        (init-role-node :system)
        (init-role-node :officer))
      (uber/add-directed-edges
        ;  Production edges
        (production-relationship :create-case :customer-id)
        (production-relationship :create-case :case-id)
        (production-relationship :consent-to-IR :consent)
        (production-relationship :fetch-supplimentary-info :customer-info)
        (production-relationship :know-your-customer :know-your-customer-data)
        (production-relationship :add-economy :economy)
        (production-relationship :get-currently-owned-real-estate :currently-owned-real-estate)
        (production-relationship :add-loan-details :loan-details)
        (production-relationship :add-collateral :collateral)
        (production-relationship :create-collateral-link :collateral-link)
        (production-relationship :calculate-amortization :amortization)
        (production-relationship :produce-credit-application-document :credit-application-reference)
        (production-relationship :sign-credit-application-document :credit-application-signature)
        ; Prerequisite edges
        (action-prerequisite :consent-to-IR :customer-id)
        (action-prerequisite :fetch-supplimentary-info :consent)
        (action-prerequisite :know-your-customer :consent)
        (action-prerequisite :add-economy :customer-id)
        (action-prerequisite :get-currently-owned-real-estate :consent)
        (action-prerequisite :add-loan-details :case-id)
        (action-prerequisite :add-collateral :case-id)
        (action-prerequisite :create-collateral-link :currently-owned-real-estate)
        (action-prerequisite :create-collateral-link :loan-details)
        (action-prerequisite :create-collateral-link :collateral)
        (action-prerequisite :calculate-amortization :economy)
        (action-prerequisite :calculate-amortization :collateral-link)
        (action-prerequisite :produce-credit-application-document :customer-info)
        (action-prerequisite :produce-credit-application-document :loan-details)
        (action-prerequisite :produce-credit-application-document :amortization)
        (action-prerequisite :produce-credit-application-document :currently-owned-real-estate)
        (action-prerequisite :produce-credit-application-document :collateral)
        (action-prerequisite :produce-credit-application-document :collateral-link)
        (action-prerequisite :sign-credit-application-document :credit-application-reference)
        ; Role-action edges
        (role-can-perform :applicant :create-case)
        (role-can-perform :applicant :consent-to-IR)
        (role-can-perform :system :fetch-supplimentary-info)
        (role-can-perform :system :know-your-customer)
        (role-can-perform :applicant :add-economy)
        (role-can-perform :system :get-currently-owned-real-estate)
        (role-can-perform :applicant :add-loan-details)
        (role-can-perform :applicant :add-collateral)
        (role-can-perform :applicant :create-collateral-link)
        (role-can-perform :system :calculate-amortization)
        (role-can-perform :applicant :sign-credit-application-document))))

(defn get-process-graph
  []
  case-graph)

(do (uber/viz-graph case-graph {:layout :fdp
                                :save   {:filename "resources/case-graph.png" :format :png}}))


(comment (uber/pprint case-graph))
(comment (uber/ubergraph->edn case-graph))

