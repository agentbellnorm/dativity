(ns dativity.demo
  (:require [dativity.core :as c]
            [dativity.define :as d]))











;; define empty case

(def case-atom (atom {}))








;; define case model with actions, data, roles and relationships

(def case-model
  (d/create-model {:actions                     [:create-case
                                                 :enter-loan-details
                                                 :produce-credit-application-document
                                                 :sign-credit-application-document
                                                 :payout-loan]
                   :data                        [:case-id
                                                 :customer-id
                                                 :loan-details
                                                 :credit-application-document
                                                 :applicant-signature
                                                 :officer-signature
                                                 :loan-number]
                   :roles                       [:applicant
                                                 :system
                                                 :officer]
                   :action-produces             [[:create-case :customer-id]
                                                 [:create-case :case-id]
                                                 [:enter-loan-details :loan-details]
                                                 [:produce-credit-application-document :credit-application-document]
                                                 [:sign-credit-application-document :applicant-signature]
                                                 [:sign-credit-application-document :officer-signature]
                                                 [:payout-loan :loan-number]]
                   :role-performs               [[:applicant :create-case]
                                                 [:applicant :enter-loan-details]
                                                 [:applicant :sign-credit-application-document]
                                                 [:officer :sign-credit-application-document]
                                                 [:system :payout-loan]
                                                 [:system :produce-credit-application-document]]
                   :action-requires             [[:enter-loan-details :case-id]
                                                 [:produce-credit-application-document :loan-details]
                                                 [:produce-credit-application-document :customer-id]
                                                 [:sign-credit-application-document :credit-application-document]
                                                 [:payout-loan :applicant-signature]
                                                 [:payout-loan :officer-signature]]
                   :action-requires-conditional []}))










;; Look at the graph

(comment (dativity.visualize/generate-png case-model))










;; see what actions can be performed based on the current (empty) case data

(c/next-actions case-model (deref case-atom))











;; What can the different roles do?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)














;; What data is produced by ':create-case'?

(c/data-produced-by-action case-model :create-case)














;; simulate action 'create-case' by adding customer-id and case-id

(swap! case-atom (fn [case]
                   (-> case
                       (c/add-data :customer-id "199209041111")
                       (c/add-data :case-id "1234"))))












;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)















;; the user enters details about the loan they want to apply for

(swap! case-atom
       (fn [case]
         (c/add-data case :loan-details {:amount  100000
                                         :purpose "dunder-honung"
                                         :product "blanco"})))














;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)











;; The system produces the credit application document

(swap! case-atom (fn [case]
                   (c/add-data case
                               :credit-application-document
                               {:document-id "abc-123"})))

















;; What actions were performed so far?

(c/actions-performed case-model (deref case-atom))











;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)











;; The officer signs the document

(swap! case-atom (fn [case]
                   (c/add-data case
                               :officer-signature
                               "Krösus Sork")))










;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)










;; The case is invalidated 'from' the enter-loan-details action and 'forward'.
;; This could be due to that the applicant navigated backwards in the UI or
;; that they changed the loan-detail data.

(swap! case-atom (fn [case]
                   (c/invalidate-data case-model case :loan-details)))










;; are the loan details gone from your state?

(:loan-details (deref case-atom))











;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)








;; Can the credit application document still be signed?

(c/action-allowed? case-model (deref case-atom) :sign-credit-application-document)









;; New data is entered, a new document is produced and
;; both the officer and applicant signs the document

(swap! case-atom
       (fn [case]
         (-> case
             (c/add-data :loan-details {:amount 150000 :purpose "dunder-honung" :product "blanco"})
             (c/add-data :credit-application-document {:document-id "cde-456"})
             (c/add-data :officer-signature "Krösus Sork")
             (c/add-data :applicant-signature "Bamse"))))












;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)








;; The loan is paid out by the system

(swap! case-atom (fn [case] (c/add-data case :loan-number "9021-3457653")))















;; No one can do anything

(c/next-actions case-model (deref case-atom))






;; What does the case look like?

(deref case-atom)







;; There is a new requirement that loans over 300 000 must be counter signed by
;; another officer before the loan kan be paid out.
;;
;; Extend the case model with this rule.

(def case-model-countersign
  (-> case-model
      (d/add-entity-to-model (d/action :counter-sign-application))
      (d/add-entity-to-model (d/data :counter-signature))
      (d/add-relationship-to-model (d/role-performs :officer :counter-sign-application))
      (d/add-relationship-to-model (d/action-produces :counter-sign-application :counter-signature))
      (d/add-relationship-to-model (d/action-requires :counter-sign-application :officer-signature))
      (d/add-relationship-to-model (d/action-requires-conditional
                                     :payout-loan
                                     :counter-signature
                                     (fn [loan-details]
                                       (> (:amount loan-details) 300000))
                                     :loan-details))))









;; Take a look at the model

(comment (v/generate-png case-model-countersign))








;; A case where the loan amount is more than 300 000:

(def big-loan-case
  (atom (-> {}
            (c/add-data :customer-id "199209041111")
            (c/add-data :case-id "1234")
            (c/add-data :loan-details {:amount 400000 :purpose "dunder-honung" :product "blanco"})
            (c/add-data :credit-application-document {:document-id "cde-456"})
            (c/add-data :officer-signature "Krösus Sork")
            (c/add-data :applicant-signature "Bamse"))))






;; A case where the loan amount is less than 300 000

(def small-loan-case
  (atom (-> {}
            (c/add-data :customer-id "199209041111")
            (c/add-data :case-id "1234")
            (c/add-data :loan-details {:amount 50000 :purpose "dunder-honung" :product "blanco"})
            (c/add-data :credit-application-document {:document-id "cde-456"})
            (c/add-data :officer-signature "Krösus Sork")
            (c/add-data :applicant-signature "Bamse"))))








;; With the 'old' model, the loan can be paid out regardless of the size of the loan

(c/next-actions case-model (deref small-loan-case))
(c/next-actions case-model (deref big-loan-case))







;; With the 'new' counter sign model, the loan can be paid out when the loan is small.
;; If the loan is big a counter signature is required first.

(c/next-actions case-model-countersign (deref small-loan-case))
(c/next-actions case-model-countersign (deref big-loan-case))







;; The document is counter signed

(swap! big-loan-case
       (fn [case]
         (c/add-data case :counter-signature "Vargen")))






;; The loan can be paid out
(c/next-actions case-model-countersign (deref big-loan-case))
