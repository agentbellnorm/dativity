(ns dativity.core-test
  (:require
    [ysera.test :refer [is= is is-not]]
    [clojure.test :refer [deftest testing]]
    [dativity.core :as c]
    [dativity.define :as d]))

(def case-graph
  (d/create-model
    {:actions                     [:create-case
                                   :consent-to-personal-data-retrieval-and-storage
                                   :fetch-supplimentary-info
                                   :know-your-customer
                                   :add-economy
                                   :get-currently-owned-real-estate
                                   :add-loan-details
                                   :add-collateral-valuation
                                   :add-collateral
                                   :create-collateral-link
                                   :calculate-amortization
                                   :produce-credit-application-document
                                   :sign-credit-application-document]
     :data                        [:case-id
                                   :customer-id
                                   :customer-info
                                   :consent
                                   :know-your-customer-data
                                   :economy
                                   :currently-owned-real-estate
                                   :loan-details
                                   :collateral
                                   :collateral-valuation
                                   :collateral-link
                                   :amortization
                                   :credit-application-reference
                                   :credit-application-signature]
     :roles                       [:applicant
                                   :system
                                   :officer]
     :action-produces             [[:add-collateral :collateral]
                                   [:add-collateral-valuation :collateral-valuation]
                                   [:add-economy :economy]
                                   [:add-loan-details :loan-details]
                                   [:calculate-amortization :amortization]
                                   [:consent-to-personal-data-retrieval-and-storage :consent]
                                   [:create-case :case-id]
                                   [:create-case :customer-id]
                                   [:create-collateral-link :collateral-link]
                                   [:fetch-supplimentary-info :customer-info]
                                   [:get-currently-owned-real-estate :currently-owned-real-estate]
                                   [:know-your-customer :know-your-customer-data]
                                   [:produce-credit-application-document :credit-application-reference]
                                   [:sign-credit-application-document :credit-application-signature]]
     :action-requires             [[:add-collateral :case-id]
                                   [:add-collateral-valuation :collateral]
                                   [:add-economy :customer-id]
                                   [:add-loan-details :case-id]
                                   [:calculate-amortization :collateral-link]
                                   [:calculate-amortization :economy]
                                   [:consent-to-personal-data-retrieval-and-storage :customer-id]
                                   [:create-collateral-link :collateral]
                                   [:create-collateral-link :currently-owned-real-estate]
                                   [:create-collateral-link :loan-details]
                                   [:fetch-supplimentary-info :consent]
                                   [:get-currently-owned-real-estate :consent]
                                   [:know-your-customer :consent]
                                   [:produce-credit-application-document :amortization]
                                   [:produce-credit-application-document :collateral-link]
                                   [:produce-credit-application-document :collateral]
                                   [:produce-credit-application-document :currently-owned-real-estate]
                                   [:produce-credit-application-document :customer-info]
                                   [:produce-credit-application-document :loan-details]
                                   [:sign-credit-application-document :credit-application-reference]]
     :role-performs               [[:applicant :add-collateral]
                                   [:applicant :add-economy]
                                   [:applicant :add-loan-details]
                                   [:applicant :consent-to-personal-data-retrieval-and-storage]
                                   [:applicant :create-case]
                                   [:applicant :create-collateral-link]
                                   [:applicant :sign-credit-application-document]
                                   [:system :calculate-amortization]
                                   [:system :fetch-supplimentary-info]
                                   [:system :get-currently-owned-real-estate]
                                   [:system :know-your-customer]]
     :action-requires-conditional [{:action             :create-collateral-link
                                    :requires           :collateral-valuation
                                    :condition          (fn [loan-details] (> (:amount loan-details) 2000000))
                                    :condition-argument :loan-details}]}))

(clojure.pprint/pprint case-graph)

(comment (dativity.visualize/generate-png case-graph))

(deftest actions-it
  (testing "runs a case through the whole flow and makes
            sure that only the right actions are available"
    (as-> {} case
          (do
            (is= (c/next-actions case-graph case) #{:create-case})
            (is= (c/next-actions case-graph case :applicant) #{:create-case})
            (is= (c/next-actions case-graph case :system) #{}) case)
          (c/add-data case :case-id "100001")
          (c/add-data case :customer-id "9209041111")
          (do
            (is= (c/next-actions case-graph case) #{:add-loan-details
                                                    :add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy}) case)
          (c/add-data case :loan-details {:amount  1000000
                                          :product "Bolån"})
          (do
            (is= (c/next-actions case-graph case) #{:add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy})
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details})
            (is-not (c/action-allowed? case-graph case :produce-credit-application-document))
            (is (c/action-allowed? case-graph case :add-loan-details)) case)
          (c/add-data case :collateral {:designation {:municipality "Täby"
                                                      :region       "Pallen"
                                                      :block        "11:45"}})
          (do
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral})
            (is-not (c/action-allowed? case-graph case :add-collateral-link)) case)
          (c/add-data case :consent {:uc  true
                                     :lmv true
                                     :pep true})
          (do
            (is= (c/next-actions case-graph case) #{:fetch-supplimentary-info
                                                    :get-currently-owned-real-estate
                                                    :add-collateral-valuation
                                                    :add-economy
                                                    :know-your-customer})
            (is-not (c/action-allowed? case-graph case :create-collateral-link)) case)
          (c/add-data case :economy {:income   500000
                                     :children 2})
          (c/add-data case :customer-info {:name "Carl-Jan Granqvist"
                                           :age  63})
          (c/add-data case :currently-owned-real-estate {:name "Villa villerkulla"})
          (do
            (is= (c/next-actions case-graph case) #{:create-collateral-link
                                                    :know-your-customer
                                                    :add-collateral-valuation}) case))))


(deftest invalidate-action-it
  (testing "Given a case that has a few actions performed, when an action is invalidated,
            then the case should be 'rewinded' to that action that was invalidated. No data should be removed."
    (as-> {} case
          (do
            (is= (c/next-actions case-graph case) #{:create-case})
            (is= (c/next-actions case-graph case :applicant) #{:create-case})
            (is= (c/next-actions case-graph case :system) #{}) case)
          (c/add-data case :case-id "100001")
          (c/add-data case :customer-id "9209041111")
          (do
            (is= (c/next-actions case-graph case) #{:add-loan-details
                                                    :add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy}) case)
          (c/add-data case :loan-details {:amount  1000000
                                          :product "Bolån"})
          (do
            (is= (c/next-actions case-graph case) #{:add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy})
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details})
            (is-not (c/action-allowed? case-graph case :produce-credit-application-document))
            (is (c/action-allowed? case-graph case :add-loan-details)) case)
          (c/add-data case :collateral {:designation {:municipality "Täby"
                                                      :region       "Pallen"
                                                      :block        "11:45"}})
          (do
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral})
            (is-not (c/action-allowed? case-graph case :add-collateral-link)) case)
          (c/add-data case :consent {:uc  true
                                     :lmv true
                                     :pep true})
          (c/add-data case :know-your-customer-data {:income 10000})
          (do
            (is= (c/next-actions case-graph case) #{:fetch-supplimentary-info
                                                    :get-currently-owned-real-estate
                                                    :add-collateral-valuation
                                                    :add-economy})
            (is-not (c/action-allowed? case-graph case :create-collateral-link)) case)
          (c/add-data case :currently-owned-real-estate {:address "Bägersta Byväg 17"})
          (do (is= (c/actions-performed case-graph case) #{:create-case
                                                           :add-loan-details
                                                           :add-collateral
                                                           :get-currently-owned-real-estate
                                                           :consent-to-personal-data-retrieval-and-storage
                                                           :know-your-customer})
              (is (c/action-allowed? case-graph case :create-collateral-link)) case)
          (c/invalidate-action case-graph case :consent-to-personal-data-retrieval-and-storage) ; INVALIDATION!!
          (do
            (is-not (c/action-allowed? case-graph case :fetch-supplimentary-info))
            (is-not (c/action-allowed? case-graph case :know-your-customer))
            (is= (c/next-actions case-graph case) #{:add-collateral-valuation
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy})
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral})
            (is-not (c/action-allowed? case-graph case :produce-credit-application-document))
            (is (c/action-allowed? case-graph case :add-loan-details))
            (is (c/case-has-data? case :loan-details))
            (is (c/case-has-data? case :collateral))
            (is (c/case-has-data? case :consent))
            (is (c/case-has-data? case :case-id))
            (is (c/case-has-data? case :customer-id))))))

(deftest invalidate-data-it
  (testing "Given a case that has a few actions performed, when a data node is invalidated,
            then the case should be 'rewinded' to the action that produced that data.
            Actions that depend on that data should be invalidated. No data should be removed."
    (as-> {} case
          (do
            (is= (c/next-actions case-graph case) #{:create-case})
            (is= (c/next-actions case-graph case :applicant) #{:create-case})
            (is= (c/next-actions case-graph case :system) #{}) case)
          (c/add-data case :case-id "100001")
          (c/add-data case :customer-id "9209041111")
          (do
            (is= (c/next-actions case-graph case) #{:add-loan-details
                                                    :add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy}) case)
          (c/add-data case :loan-details {:amount  1000000
                                          :product "Bolån"})
          (do
            (is= (c/next-actions case-graph case) #{:add-collateral
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy})
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details})
            (is-not (c/action-allowed? case-graph case :produce-credit-application-document))
            (is (c/action-allowed? case-graph case :add-loan-details)) case)
          (c/add-data case :collateral {:designation {:municipality "Täby"
                                                      :region       "Pallen"
                                                      :block        "11:45"}})
          (do
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral})
            (is-not (c/action-allowed? case-graph case :add-collateral-link)) case)
          (c/add-data case :consent {:uc  true
                                     :lmv true
                                     :pep true})
          (c/add-data case :know-your-customer-data {:income 10000})
          (do
            (is= (c/next-actions case-graph case) #{:fetch-supplimentary-info
                                                    :get-currently-owned-real-estate
                                                    :add-collateral-valuation
                                                    :add-economy})
            (is-not (c/action-allowed? case-graph case :create-collateral-link)) case)
          (c/add-data case :currently-owned-real-estate {:address "Bägersta Byväg 17"})
          (do (is= (c/actions-performed case-graph case) #{:create-case
                                                           :add-loan-details
                                                           :add-collateral
                                                           :get-currently-owned-real-estate
                                                           :consent-to-personal-data-retrieval-and-storage
                                                           :know-your-customer})
              (is (c/action-allowed? case-graph case :create-collateral-link)) case)
          (c/invalidate-data case-graph case :consent)      ; INVALIDATION!!
          (do
            (is-not (c/action-allowed? case-graph case :fetch-supplimentary-info))
            (is-not (c/action-allowed? case-graph case :know-your-customer))
            (is-not (c/action-allowed? case-graph case :get-currently-owned-real-estate))
            (is-not (c/action-allowed? case-graph case :produce-credit-application-document))
            (is= (c/next-actions case-graph case) #{:add-collateral-valuation
                                                    :consent-to-personal-data-retrieval-and-storage
                                                    :add-economy})
            (is= (c/actions-performed case-graph case) #{:create-case :add-loan-details :add-collateral})
            (is (c/action-allowed? case-graph case :add-loan-details))
            (is (c/case-has-data? case :loan-details))
            (is (c/case-has-data? case :collateral))
            (is (c/case-has-data? case :consent))
            (is (c/case-has-data? case :case-id))
            (is (c/case-has-data? case :customer-id))))))

(deftest conditional-it
  (testing "When the loan amount is over 2 000 000 then the collateral needs to have a valuation.
  If the loan amount is lower than or equal to 2 000 000 then it's possible to proceed and create
  the collateral-link without a valuation. It's still possible to add it, but not required."
    (do (as-> {} case
              (c/add-data case :case-id "100001")
              (c/add-data case :customer-id "9209041111")
              (c/add-data case :consent {:uc true :lmv true :pep true})
              (c/add-data case :currently-owned-real-estate {:name "Villa villerkulla"})
              (c/add-data case :loan-details {:amount 1000000 :product "Bolån"})
              (c/add-data case :collateral {:designation {:municipality "Täby"
                                                          :region       "Pallen"
                                                          :block        "11:45"}})
              (do
                (is (c/action-allowed? case-graph case :create-collateral-link))))
        (as-> {} case
              (c/add-data case :case-id "100001")
              (c/add-data case :customer-id "9209041111")
              (c/add-data case :consent {:uc true :lmv true :pep true})
              (c/add-data case :currently-owned-real-estate {:name "Villa villerkulla"})
              (c/add-data case :loan-details {:amount 3000000 :product "Bolån"})
              (c/add-data case :collateral {:designation {:municipality "Täby"
                                                          :region       "Pallen"
                                                          :block        "11:45"}})
              (do
                (is-not (c/action-allowed? case-graph case :create-collateral-link))
                case)
              (c/add-data case :collateral-valuation {:valuation 5700000
                                                      :valuator  "Karl Anka"})
              (is (c/action-allowed? case-graph case :create-collateral-link))))))
