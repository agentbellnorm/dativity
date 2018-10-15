(ns dativity.core-test
  (:require [clojure.test :refer :all]
            [dativity.core :refer :all]
            [dativity.define :as define]))

(def case-graph
  (-> (define/create-empty-case)
      ; Actions
      (define/add-node-to-case (define/action :create-case))
      (define/add-node-to-case (define/action :consent-to-IR))
      (define/add-node-to-case (define/action :fetch-supplimentary-info))
      (define/add-node-to-case (define/action :know-your-customer))
      (define/add-node-to-case (define/action :add-economy))
      (define/add-node-to-case (define/action :get-currently-owned-real-estate))
      (define/add-node-to-case (define/action :add-loan-details))
      (define/add-node-to-case (define/action :add-collateral))
      (define/add-node-to-case (define/action :create-collateral-link))
      (define/add-node-to-case (define/action :calculate-amortization))
      (define/add-node-to-case (define/action :produce-credit-application-document))
      (define/add-node-to-case (define/action :sign-credit-application-document))
      ; Data entities
      (define/add-node-to-case (define/data :case-id))
      (define/add-node-to-case (define/data :customer-id))
      (define/add-node-to-case (define/data :customer-info))
      (define/add-node-to-case (define/data :consent))
      (define/add-node-to-case (define/data :know-your-customer-data))
      (define/add-node-to-case (define/data :economy))
      (define/add-node-to-case (define/data :currently-owned-real-estate))
      (define/add-node-to-case (define/data :loan-details))
      (define/add-node-to-case (define/data :collateral))
      (define/add-node-to-case (define/data :collateral-link))
      (define/add-node-to-case (define/data :amortization))
      (define/add-node-to-case (define/data :credit-application-reference))
      (define/add-node-to-case (define/data :credit-application-signature))
      ; Roles
      (define/add-node-to-case (define/role :applicant))
      (define/add-node-to-case (define/role :system))
      (define/add-node-to-case (define/role :officer))
      ;  Production edges
      (define/add-relationship-to-case (define/production-relationship :create-case :customer-id))
      (define/add-relationship-to-case (define/production-relationship :create-case :case-id))
      (define/add-relationship-to-case (define/production-relationship :consent-to-IR :consent))
      (define/add-relationship-to-case (define/production-relationship :fetch-supplimentary-info :customer-info))
      (define/add-relationship-to-case (define/production-relationship :know-your-customer :know-your-customer-data))
      (define/add-relationship-to-case (define/production-relationship :add-economy :economy))
      (define/add-relationship-to-case (define/production-relationship :get-currently-owned-real-estate :currently-owned-real-estate))
      (define/add-relationship-to-case (define/production-relationship :add-loan-details :loan-details))
      (define/add-relationship-to-case (define/production-relationship :add-collateral :collateral))
      (define/add-relationship-to-case (define/production-relationship :create-collateral-link :collateral-link))
      (define/add-relationship-to-case (define/production-relationship :calculate-amortization :amortization))
      (define/add-relationship-to-case (define/production-relationship :produce-credit-application-document :credit-application-reference))
      (define/add-relationship-to-case (define/production-relationship :sign-credit-application-document :credit-application-signature))
      ; Prerequisite edges
      (define/add-relationship-to-case (define/action-prerequisite :consent-to-IR :customer-id))
      (define/add-relationship-to-case (define/action-prerequisite :fetch-supplimentary-info :consent))
      (define/add-relationship-to-case (define/action-prerequisite :know-your-customer :consent))
      (define/add-relationship-to-case (define/action-prerequisite :add-economy :customer-id))
      (define/add-relationship-to-case (define/action-prerequisite :get-currently-owned-real-estate :consent))
      (define/add-relationship-to-case (define/action-prerequisite :add-loan-details :case-id))
      (define/add-relationship-to-case (define/action-prerequisite :add-collateral :case-id))
      (define/add-relationship-to-case (define/action-prerequisite :create-collateral-link :currently-owned-real-estate))
      (define/add-relationship-to-case (define/action-prerequisite :create-collateral-link :loan-details))
      (define/add-relationship-to-case (define/action-prerequisite :create-collateral-link :collateral))
      (define/add-relationship-to-case (define/action-prerequisite :calculate-amortization :economy))
      (define/add-relationship-to-case (define/action-prerequisite :calculate-amortization :collateral-link))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :customer-info))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :loan-details))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :amortization))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :currently-owned-real-estate))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :collateral))
      (define/add-relationship-to-case (define/action-prerequisite :produce-credit-application-document :collateral-link))
      (define/add-relationship-to-case (define/action-prerequisite :sign-credit-application-document :credit-application-reference))
      ; Role-action edges
      (define/add-relationship-to-case (define/role-can-perform :applicant :create-case))
      (define/add-relationship-to-case (define/role-can-perform :applicant :consent-to-IR))
      (define/add-relationship-to-case (define/role-can-perform :system :fetch-supplimentary-info))
      (define/add-relationship-to-case (define/role-can-perform :system :know-your-customer))
      (define/add-relationship-to-case (define/role-can-perform :applicant :add-economy))
      (define/add-relationship-to-case (define/role-can-perform :system :get-currently-owned-real-estate))
      (define/add-relationship-to-case (define/role-can-perform :applicant :add-loan-details))
      (define/add-relationship-to-case (define/role-can-perform :applicant :add-collateral))
      (define/add-relationship-to-case (define/role-can-perform :applicant :create-collateral-link))
      (define/add-relationship-to-case (define/role-can-perform :system :calculate-amortization))
      (define/add-relationship-to-case (define/role-can-perform :applicant :sign-credit-application-document))))

(define/generate-graph-image! case-graph "resources/case-graph.png")

(deftest actions-it
  (testing "runs a case through the whole flow and makes
            sure that only the right actions are available"
    (as-> {} case
          (do
            (is (= (next-actions case-graph case) #{:create-case}))
            (is (= (next-actions case-graph case :applicant) #{:create-case}))
            (is (= (next-actions case-graph case :system) #{})) case)
          (add-data-to-case case :case-id "100001")
          (add-data-to-case case :customer-id "9209041111")
          (do
            (is (= (next-actions case-graph case) #{:add-loan-details :add-collateral :consent-to-IR :add-economy})) case)
          (add-data-to-case case :loan-details {:amount  "1000000"
                                                :product "Bolån"})
          (do
            (is (= (next-actions case-graph case) #{:add-collateral :consent-to-IR :add-economy}))
            (is (= (actions-performed case-graph case) #{:create-case :add-loan-details}))
            (is (not (action-allowed? case-graph case :produce-credit-application-document)))
            (is (action-allowed? case-graph case :add-loan-details)) case)
          (add-data-to-case case :collateral {:designation {:municipality "Täby"
                                                            :region       "Pallen"
                                                            :block        "11:45"}
                                              :valuation   "5700000"})
          (do
            (is (= (actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral}))
            (is (not (action-allowed? case-graph case :add-collateral-link))) case)
          (add-data-to-case case :consent {:uc  true
                                           :lmv true
                                           :pep true})
          (do
            (is (= (next-actions case-graph case) #{:fetch-supplimentary-info
                                                       :get-currently-owned-real-estate
                                                       :add-economy
                                                       :know-your-customer}))
            (is (false? (action-allowed? case-graph case :create-collateral-link))) case)
          (add-data-to-case case :economy {:income   500000
                                           :children 2})
          (add-data-to-case case :customer-info {:name "Carl-Jan Granqvist"
                                                 :age  63})
          (add-data-to-case case :currently-owned-real-estate {:name "Villa villerkulla"})
          (do
            (is (= (next-actions case-graph case) #{:create-collateral-link :know-your-customer}))))
    ))
