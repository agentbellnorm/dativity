(ns dativity.core-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [ubergraph.core :as uber]
            [clojure.java.io :as io]
            [dativity.core :refer :all]))

(def process-graph
  (-> (io/resource "test-process.edn")
      slurp
      edn/read-string
      uber/edn->ubergraph))

(defn add-data-to-case
  {:test (fn []
           (is (= (add-data-to-case {} {:case-id 3}) {:case-id 3} ))
           (is (= (add-data-to-case {:case-id 3 :customer-id "920904"} {:loannumber "90291234567"})
                  {:case-id 3 :customer-id "920904" :loannumber "90291234567"}))
           (is (= (add-data-to-case {:case-id 3 :customer-id "920904" :loannumber "90291234567"} {:loan-details {:amount "1000000" :product "Bolån"}})
                  {:case-id 3 :customer-id "920904" :loannumber "90291234567" :loan-details {:amount  "1000000"
                                                                                             :product "Bolån"}}))
           )}
  [case data]
  (assoc case (first (keys data)) (first (vals data))))


(deftest actions-it
  (testing "runs a case through the whole flow and makes
            sure that only the right actions are available"
             (as-> {} case
                   (do
                     (is (= (next-actions process-graph case) #{:create-case}))
                     (is (= (next-actions process-graph case :applicant) #{:create-case}))
                     (is (= (next-actions process-graph case :system) #{})) case)
                   (add-data-to-case case {:case-id "100001"})
                   (add-data-to-case case {:customer-id "9209041111"})
                   (do
                     (is (= (next-actions process-graph case) #{:add-loan-details :add-collateral :consent-to-IR :add-economy})) case)
                   (add-data-to-case case {:loan-details {:amount  "1000000"
                                                          :product "Bolån"}})
                   (do
                     (is (= (next-actions process-graph case) #{:add-collateral :consent-to-IR :add-economy}))
                     (is (= (actions-performed process-graph case) #{:create-case :add-loan-details}))
                     (is (not (action-allowed? process-graph case :produce-credit-application-document)))
                     (is (action-allowed? process-graph case :add-loan-details)) case)
                   (add-data-to-case case {:collateral {:designation {:municipality "Täby"
                                                                      :region "Pallen"
                                                                      :block "11:45"}
                                                        :valuation "5700000"}})
                   (do
                     (is (= (actions-performed process-graph case) #{:create-case :add-loan-details :add-collateral}))
                     (is (not (action-allowed? process-graph case :add-collateral-link))) case)
                   (add-data-to-case case {:consent {:uc  true
                                                     :lmv true
                                                     :pep true}})
                   (do
                     (is (= (next-actions process-graph case) #{:fetch-supplimentary-info
                                                                :get-currently-owned-real-estate
                                                                :add-economy
                                                                :know-your-customer}))
                     (is (false? (action-allowed? process-graph case :create-collateral-link))) case)
                   (add-data-to-case case {:economy {:income 500000
                                                     :children 2}})
                   (add-data-to-case case {:customer-info {:name "Carl-Jan Granqvist"
                                                           :age 63}})
                   (add-data-to-case case {:currently-owned-real-estate {:name "Villa villerkulla"}})
                   (do
                     (is (= (next-actions process-graph case) #{:create-collateral-link :know-your-customer}))))
             ))
