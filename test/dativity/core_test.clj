(ns dativity.core-test
  (:require [clojure.test :refer :all]
            [dativity.core :as c]
            [dativity.define :as d]))

(defn printreturn [x] (println x) x)

(def case-graph
  (-> (d/empty-case-model)
      ; Actions
      (d/add-entity-to-model (d/action :create-case))
      (d/add-entity-to-model (d/action :consent-to-personal-data-retrieval-and-storage))
      (d/add-entity-to-model (d/action :fetch-supplimentary-info))
      (d/add-entity-to-model (d/action :know-your-customer))
      (d/add-entity-to-model (d/action :add-economy))
      (d/add-entity-to-model (d/action :get-currently-owned-real-estate))
      (d/add-entity-to-model (d/action :add-loan-details))
      (d/add-entity-to-model (d/action :add-collateral))
      (d/add-entity-to-model (d/action :create-collateral-link))
      (d/add-entity-to-model (d/action :calculate-amortization))
      (d/add-entity-to-model (d/action :produce-credit-application-document))
      (d/add-entity-to-model (d/action :sign-credit-application-document))
      ; Data entities
      (d/add-entity-to-model (d/data :case-id))
      (d/add-entity-to-model (d/data :customer-id))
      (d/add-entity-to-model (d/data :customer-info))
      (d/add-entity-to-model (d/data :consent))
      (d/add-entity-to-model (d/data :know-your-customer-data))
      (d/add-entity-to-model (d/data :economy))
      (d/add-entity-to-model (d/data :currently-owned-real-estate))
      (d/add-entity-to-model (d/data :loan-details))
      (d/add-entity-to-model (d/data :collateral))
      (d/add-entity-to-model (d/data :collateral-link))
      (d/add-entity-to-model (d/data :amortization))
      (d/add-entity-to-model (d/data :credit-application-reference))
      (d/add-entity-to-model (d/data :credit-application-signature))
      ; Roles
      (d/add-entity-to-model (d/role :applicant))
      (d/add-entity-to-model (d/role :system))
      (d/add-entity-to-model (d/role :officer))
      ;  Production edges
      (d/add-relationship-to-model (d/action-produces :create-case :customer-id))
      (d/add-relationship-to-model (d/action-produces :create-case :case-id))
      (d/add-relationship-to-model (d/action-produces :consent-to-personal-data-retrieval-and-storage :consent))
      (d/add-relationship-to-model (d/action-produces :fetch-supplimentary-info :customer-info))
      (d/add-relationship-to-model (d/action-produces :know-your-customer :know-your-customer-data))
      (d/add-relationship-to-model (d/action-produces :add-economy :economy))
      (d/add-relationship-to-model (d/action-produces :get-currently-owned-real-estate :currently-owned-real-estate))
      (d/add-relationship-to-model (d/action-produces :add-loan-details :loan-details))
      (d/add-relationship-to-model (d/action-produces :add-collateral :collateral))
      (d/add-relationship-to-model (d/action-produces :create-collateral-link :collateral-link))
      (d/add-relationship-to-model (d/action-produces :calculate-amortization :amortization))
      (d/add-relationship-to-model (d/action-produces :produce-credit-application-document :credit-application-reference))
      (d/add-relationship-to-model (d/action-produces :sign-credit-application-document :credit-application-signature))
      ; Prerequisite edges
      (d/add-relationship-to-model (d/action-requires :consent-to-personal-data-retrieval-and-storage :customer-id))
      (d/add-relationship-to-model (d/action-requires :fetch-supplimentary-info :consent))
      (d/add-relationship-to-model (d/action-requires :know-your-customer :consent))
      (d/add-relationship-to-model (d/action-requires :add-economy :customer-id))
      (d/add-relationship-to-model (d/action-requires :get-currently-owned-real-estate :consent))
      (d/add-relationship-to-model (d/action-requires :add-loan-details :case-id))
      (d/add-relationship-to-model (d/action-requires :add-collateral :case-id))
      (d/add-relationship-to-model (d/action-requires :create-collateral-link :currently-owned-real-estate))
      (d/add-relationship-to-model (d/action-requires :create-collateral-link :loan-details))
      (d/add-relationship-to-model (d/action-requires :create-collateral-link :collateral))
      (d/add-relationship-to-model (d/action-requires :calculate-amortization :economy))
      (d/add-relationship-to-model (d/action-requires :calculate-amortization :collateral-link))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :customer-info))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :loan-details))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :amortization))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :currently-owned-real-estate))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :collateral))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :collateral-link))
      (d/add-relationship-to-model (d/action-requires :sign-credit-application-document :credit-application-reference))
      ; Role-action edges
      (d/add-relationship-to-model (d/role-performs :applicant :create-case))
      (d/add-relationship-to-model (d/role-performs :applicant :consent-to-personal-data-retrieval-and-storage))
      (d/add-relationship-to-model (d/role-performs :system :fetch-supplimentary-info))
      (d/add-relationship-to-model (d/role-performs :system :know-your-customer))
      (d/add-relationship-to-model (d/role-performs :applicant :add-economy))
      (d/add-relationship-to-model (d/role-performs :system :get-currently-owned-real-estate))
      (d/add-relationship-to-model (d/role-performs :applicant :add-loan-details))
      (d/add-relationship-to-model (d/role-performs :applicant :add-collateral))
      (d/add-relationship-to-model (d/role-performs :applicant :create-collateral-link))
      (d/add-relationship-to-model (d/role-performs :system :calculate-amortization))
      (d/add-relationship-to-model (d/role-performs :applicant :sign-credit-application-document))))

(d/generate-graph-image! case-graph "resources/case-graph.png")

(deftest actions-it
  (testing "runs a case through the whole flow and makes
            sure that only the right actions are available"
    (as-> {} case
          (do
            (is (= (c/next-actions case-graph case) #{:create-case}))
            (is (= (c/next-actions case-graph case :applicant) #{:create-case}))
            (is (= (c/next-actions case-graph case :system) #{})) case)
          (c/add-data-to-case case :case-id "100001")
          (c/add-data-to-case case :customer-id "9209041111")
          (do
            (is (= (c/next-actions case-graph case) #{:add-loan-details
                                                      :add-collateral
                                                      :consent-to-personal-data-retrieval-and-storage
                                                      :add-economy})) case)
          (c/add-data-to-case case :loan-details {:amount  "1000000"
                                                  :product "Bolån"})
          (do
            (is (= (c/next-actions case-graph case) #{:add-collateral
                                                      :consent-to-personal-data-retrieval-and-storage
                                                      :add-economy}))
            (is (= (c/actions-performed case-graph case) #{:create-case :add-loan-details}))
            (is (not (c/action-allowed? case-graph case :produce-credit-application-document)))
            (is (c/action-allowed? case-graph case :add-loan-details)) case)
          (c/add-data-to-case case :collateral {:designation {:municipality "Täby"
                                                              :region       "Pallen"
                                                              :block        "11:45"}
                                                :valuation   "5700000"})
          (do
            (is (= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral}))
            (is (not (c/action-allowed? case-graph case :add-collateral-link))) case)
          (c/add-data-to-case case :consent {:uc  true
                                             :lmv true
                                             :pep true})
          (do
            (is (= (c/next-actions case-graph case) #{:fetch-supplimentary-info
                                                      :get-currently-owned-real-estate
                                                      :add-economy
                                                      :know-your-customer}))
            (is (false? (c/action-allowed? case-graph case :create-collateral-link))) case)
          (c/add-data-to-case case :economy {:income   500000
                                             :children 2})
          (c/add-data-to-case case :customer-info {:name "Carl-Jan Granqvist"
                                                   :age  63})
          (c/add-data-to-case case :currently-owned-real-estate {:name "Villa villerkulla"})
          (do
            (is (= (c/next-actions case-graph case) #{:create-collateral-link :know-your-customer})) case))
    ))


(deftest invalidate-it
  (testing "Given a case that has a few actions performed, when an action is invalidated,
            then the case should be 'rewinded' to that action that was invalidated. No data should be removed."
    (as-> {} case
          (do
            (is (= (c/next-actions case-graph case) #{:create-case}))
            (is (= (c/next-actions case-graph case :applicant) #{:create-case}))
            (is (= (c/next-actions case-graph case :system) #{})) case)
          (c/add-data-to-case case :case-id "100001")
          (c/add-data-to-case case :customer-id "9209041111")
          (do
            (is (= (c/next-actions case-graph case) #{:add-loan-details
                                                      :add-collateral
                                                      :consent-to-personal-data-retrieval-and-storage
                                                      :add-economy})) case)
          (c/add-data-to-case case :loan-details {:amount  "1000000"
                                                  :product "Bolån"})
          (do
            (is (= (c/next-actions case-graph case) #{:add-collateral
                                                      :consent-to-personal-data-retrieval-and-storage
                                                      :add-economy}))
            (is (= (c/actions-performed case-graph case) #{:create-case :add-loan-details}))
            (is (not (c/action-allowed? case-graph case :produce-credit-application-document)))
            (is (c/action-allowed? case-graph case :add-loan-details)) case)
          (c/add-data-to-case case :collateral {:designation {:municipality "Täby"
                                                              :region       "Pallen"
                                                              :block        "11:45"}
                                                :valuation   "5700000"})
          (do
            (is (= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral}))
            (is (not (c/action-allowed? case-graph case :add-collateral-link))) case)
          (c/add-data-to-case case :consent {:uc  true
                                             :lmv true
                                             :pep true})
          (do
            (is (= (c/next-actions case-graph case) #{:fetch-supplimentary-info
                                                      :get-currently-owned-real-estate
                                                      :add-economy
                                                      :know-your-customer}))
            (is (false? (c/action-allowed? case-graph case :create-collateral-link))) case)
          (c/invalidate-action case-graph case :add-loan-details) ; INVALIDATION!!
          (do
            (is (false? (c/action-allowed? case-graph case :get-currently-owned-real-estate)))
            (is (false? (c/action-allowed? case-graph case :fetch-supplimentary-info)))
            (is (false? (c/action-allowed? case-graph case :know-your-customer)))
            (is (= (c/next-actions case-graph case) #{:add-loan-details
                                                      :add-collateral
                                                      :consent-to-personal-data-retrieval-and-storage
                                                      :add-economy}))
            (is (= (c/actions-performed case-graph case) #{:create-case}))
            (is (not (c/action-allowed? case-graph case :produce-credit-application-document)))
            (is (c/action-allowed? case-graph case :add-loan-details)))
          case)
    ))

