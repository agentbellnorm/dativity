(ns dativity.demo
  (:require [dativity.core :as c]
            [dativity.define :as d]))











;; define empty case

(def case-atom (atom {}))








;; define case model with actions, data, roles and relationships

(def case-model
  (-> (d/empty-case-model)
      ; Actions
      (d/add-entity-to-model (d/action :create-case))
      (d/add-entity-to-model (d/action :enter-loan-details))
      (d/add-entity-to-model (d/action :produce-credit-application-document))
      (d/add-entity-to-model (d/action :sign-credit-application-document))
      (d/add-entity-to-model (d/action :sign-credit-application-document))
      (d/add-entity-to-model (d/action :counter-sign-application))
      (d/add-entity-to-model (d/action :payout-loan))
      ; Data entities
      (d/add-entity-to-model (d/data :case-id))
      (d/add-entity-to-model (d/data :customer-id))
      (d/add-entity-to-model (d/data :loan-details))
      (d/add-entity-to-model (d/data :credit-application-document))
      (d/add-entity-to-model (d/data :applicant-credit-application-document-signature))
      (d/add-entity-to-model (d/data :officer-credit-application-document-signature))
      (d/add-entity-to-model (d/data :loan-number))
      (d/add-entity-to-model (d/data :counter-signature))
      ; Roles
      (d/add-entity-to-model (d/role :applicant))
      (d/add-entity-to-model (d/role :system))
      (d/add-entity-to-model (d/role :officer))
      ; Production edges
      (d/add-relationship-to-model (d/action-produces :create-case :customer-id))
      (d/add-relationship-to-model (d/action-produces :create-case :case-id))
      (d/add-relationship-to-model (d/action-produces :enter-loan-details :loan-details))
      (d/add-relationship-to-model (d/action-produces :produce-credit-application-document :credit-application-document))
      (d/add-relationship-to-model (d/action-produces :sign-credit-application-document :applicant-credit-application-document-signature))
      (d/add-relationship-to-model (d/action-produces :sign-credit-application-document :officer-credit-application-document-signature))
      (d/add-relationship-to-model (d/action-produces :payout-loan :loan-number))
      (d/add-relationship-to-model (d/action-produces :counter-sign-application :counter-signature))
      ; Prerequisite edges
      (d/add-relationship-to-model (d/action-requires :enter-loan-details :case-id))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :loan-details))
      (d/add-relationship-to-model (d/action-requires :produce-credit-application-document :customer-id))
      (d/add-relationship-to-model (d/action-requires :sign-credit-application-document :credit-application-document))
      (d/add-relationship-to-model (d/action-requires :payout-loan :applicant-credit-application-document-signature))
      (d/add-relationship-to-model (d/action-requires :payout-loan :officer-credit-application-document-signature))
      (d/add-relationship-to-model (d/action-requires-conditional :payout-loan :counter-signature (fn [loan-info]
                                                                                                    (= 1 1)) :loan-details))
      ; Role-action edges
      (d/add-relationship-to-model (d/role-performs :applicant :create-case))
      (d/add-relationship-to-model (d/role-performs :applicant :enter-loan-details))
      (d/add-relationship-to-model (d/role-performs :applicant :sign-credit-application-document))
      (d/add-relationship-to-model (d/role-performs :officer :sign-credit-application-document))
      (d/add-relationship-to-model (d/role-performs :officer :counter-sign-application))
      (d/add-relationship-to-model (d/role-performs :system :payout-loan))
      (d/add-relationship-to-model (d/role-performs :system :produce-credit-application-document))))









;; Look at the graph

(d/show-graph-image! case-model)










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
                       (c/add-data-to-case :customer-id "199209041111")
                       (c/add-data-to-case :case-id "1234"))))












;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)















;; the user enters details about the loan they want to apply for

(swap! case-atom (fn [case] (c/add-data-to-case case :loan-details {:amount  100000
                                                                    :purpose "car"
                                                                    :product "blanco"})))














;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)











;; The system produces the credit application document

(swap! case-atom (fn [case]
                   (c/add-data-to-case case
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
                   (c/add-data-to-case case
                                       :officer-credit-application-document-signature
                                       "Krösus Sork")))










;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)










;; The case is invalidated 'from' the enter-loan-details action and 'forward'.
;; This could be due to that the applicant navigated backwards in the UI or
;; that they changed the loan-detail data.

(swap! case-atom (fn [case]
                   (c/invalidate-action case-model case :enter-loan-details)))












;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)








;; Can the credit application document still be signed?

(c/action-allowed? case-model (deref case-atom) :sign-credit-application-document)









;; New data is entered, a new document is produced and
;; both the officer and applicant signs the document

(swap! case-atom (fn [case]
                   (c/add-data-to-case
                     case
                     :loan-details {:amount  150000
                                    :purpose "car"
                                    :product "blanco"})))

(swap! case-atom (fn [case]
                   (c/add-data-to-case
                     case
                     :credit-application-document {:document-id
                                                   "cde-456"})))

(swap! case-atom (fn [case]
                   (c/add-data-to-case
                     case
                     :officer-credit-application-document-signature "Krösus Sork")))

(swap! case-atom (fn [case]
                   (c/add-data-to-case
                     case
                     :applicant-credit-application-document-signature "Bamse")))












;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)








;; The loan is paid out by the system

(swap! case-atom (fn [case] (c/add-data-to-case case :loan-number "9021-3457653")))















;; What can the different roles do now?

(c/next-actions case-model (deref case-atom) :applicant)
(c/next-actions case-model (deref case-atom) :system)
(c/next-actions case-model (deref case-atom) :officer)






;; What does the case look like?

(deref case-atom)

