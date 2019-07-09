(ns dativity.define-test
  (:require [clojure.test :refer [deftest testing]]
            [ysera.test :refer [is= is is-not error?]]
            [dativity.define :refer [create-model action-requires-conditional]]))

(deftest create-model-spec-validations
  (testing "the input has to have the right keys"
    (is (error? (create-model {}))))

  (testing "the keys are required, and they have to be vectors"
    (is (create-model {:actions                     []
                       :data                        []
                       :roles                       []
                       :action-produces             []
                       :action-requires             []
                       :action-requires-conditional []
                       :role-performs               []}))

    (is (error? (create-model {:actions                     #{}
                               :data                        ""
                               :roles                       {}
                               :action-produces             []
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "actions, data, and roles have to be vectors of keywords"
    (is (error? (create-model {:actions                     ["string"]
                               :data                        [123]
                               :roles                       [true]
                               :action-produces             []
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "action-produces, action-requires and role-performs have to be tuples of keywords"
    (is (error? (create-model {:actions                     [:marco]
                               :data                        [:polo]
                               :roles                       []
                               :action-produces             []
                               :action-requires             [[:marco :polo :achtung]]
                               :role-performs               []
                               :action-requires-conditional []}))))

  (testing "action-requires-conditional"
    (testing "maps in the vector has to be of right structure"
      (is (error? (create-model {:actions                     [:flash]
                                 :data                        [:thunder :marco]
                                 :roles                       []
                                 :action-produces             []
                                 :action-requires             []
                                 :role-performs               []
                                 :action-requires-conditional [{:action             :flash
                                                                :requires           :thunder
                                                                :condition-argument :marco}
                                                               ]}))))

    (testing "condition has to be a function"
      (is (error? (create-model {:actions                     [:flash]
                                 :data                        [:thunder :marco]
                                 :roles                       []
                                 :action-produces             []
                                 :action-requires             []
                                 :role-performs               []
                                 :action-requires-conditional [{:action             :flash
                                                                :requires           :thunder
                                                                :condition          :polo
                                                                :condition-argument :marco}]}))))))

(deftest business-rules-validation
  (testing "action-produces. data must exist"
    (is (error? (create-model {:actions                     [:marco]
                               :data                        []
                               :roles                       []
                               :action-produces             [[:marco :polo]]
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "action-produces. action must exist"
    (is (error? (create-model {:actions                     []
                               :data                        [:polo]
                               :roles                       []
                               :action-produces             [[:marco :polo]]
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "action-requires. action must exist"
    (is (error? (create-model {:actions                     [:marco]
                               :data                        []
                               :roles                       []
                               :action-produces             []
                               :action-requires             [[:marco :polo]]
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "action-requires. data must exist"
    (is (error? (create-model {:actions                     []
                               :data                        [:polo]
                               :roles                       []
                               :action-produces             []
                               :action-requires             [[:marco :polo]]
                               :action-requires-conditional []
                               :role-performs               []}))))

  (testing "role-performs. role must exist"
    (is (error? (create-model {:actions                     [:marco]
                               :data                        []
                               :roles                       []
                               :action-produces             []
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               [[:polo :marco]]}))))

  (testing "role-performs. action must exist"
    (is (error? (create-model {:actions                     []
                               :data                        []
                               :roles                       [:polo]
                               :action-produces             []
                               :action-requires             []
                               :action-requires-conditional []
                               :role-performs               [[:polo :marco]]}))))

  (testing "action-requires-conditional. action must exist"
    (is (error? (create-model {:actions                     []
                               :data                        [:fnatte :tjatte]
                               :roles                       []
                               :action-produces             []
                               :action-requires             []
                               :role-performs               []
                               :action-requires-conditional [{:action             :knatte
                                                              :requires           :fnatte
                                                              :condition          some?
                                                              :condition-argument :tjatte}]}))))

  (testing "action-requires-conditional. data must exist"
    (is (error? (create-model {:actions                     [:knatte]
                               :data                        [:tjatte]
                               :roles                       []
                               :action-produces             []
                               :action-requires             []
                               :role-performs               []
                               :action-requires-conditional [{:action             :knatte
                                                              :requires           :fnatte
                                                              :condition          some?
                                                              :condition-argument :tjatte}]}))))

  (testing "action-requires-conditional. data must exist"
    (is (error? (create-model {:actions                     [:knatte]
                               :data                        [:fnatte]
                               :roles                       []
                               :action-produces             []
                               :action-requires             []
                               :role-performs               []
                               :action-requires-conditional [{:action             :knatte
                                                              :requires           :fnatte
                                                              :condition          some?
                                                              :condition-argument :tjatte}]})))))

(deftest create-model-happy-flow
  (testing "a realistic input to create-model"
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
                                                     [:me :call-grandma]]}))))
