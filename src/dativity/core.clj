(ns dativity.core
  (:require [dativity.define :as define]
            [ubergraph.core :as uber]
            [clojure.test :refer :all]
            [clojure.set :refer :all]))

(defn printreturn [x] (println x) x)

(defn test-case-definition
  []
  (-> (define/create-empty-case)
      (define/add-node-to-case (define/action :a))
      (define/add-node-to-case (define/action :d))
      (define/add-node-to-case (define/action :f))
      (define/add-node-to-case (define/role :b))
      (define/add-node-to-case (define/role :c))
      (define/add-node-to-case (define/role :e))
      (define/add-relationship-to-case (define/action-prerequisite :a :b))
      (define/add-relationship-to-case (define/action-prerequisite :a :e))
      (define/add-relationship-to-case (define/action-prerequisite :b :e))
      (define/add-relationship-to-case (define/action-prerequisite :d :e))
      (define/add-relationship-to-case (define/production-relationship :b :c))
      (define/add-relationship-to-case (define/production-relationship :d :a))
      (define/add-relationship-to-case (define/production-relationship :f :c))
      (define/add-relationship-to-case (define/role-can-perform :a :c))
      (define/add-relationship-to-case (define/role-can-perform :c :d))))

(comment (uber/viz-graph (test-case-definition)))

(defn add-data-to-case
  {:test (fn []
           (is (= (add-data-to-case {} :case-id 3)
                  {:case-id {:committed true :value 3}}))
           (is (= (add-data-to-case {:case-id     {:value 3 :committed true}
                                     :customer-id {:committed true :value "920904"}}
                                    :loan-number "90291234567")
                  {:case-id     {:committed true :value 3}
                   :customer-id {:committed true :value "920904"}
                   :loan-number {:committed true :value "90291234567"}}))
           (is (= (add-data-to-case {:case-id     {:committed true :value 3}
                                     :customer-id {:committed true :value "920904"}
                                     :loan-number {:committed true :value "90291234567"}}
                                    :loan-details {:amount "1000000" :product "Bolån"})
                  {:case-id      {:committed true :value 3}
                   :customer-id  {:committed true :value "920904"}
                   :loan-number  {:committed true :value "90291234567"}
                   :loan-details {:committed true :value {:product "Bolån" :amount "1000000"}}}))
           )}
  [case key value]
  (assoc case key {:committed true
                   :value     value}))

(defn all-actions
  {:test (fn []
           (is (= (all-actions (test-case-definition)) [:a :d :f])))}
  [process-definition]
  (->> (uber/nodes process-definition)
       (filter (fn [node] (= :action (uber/attr process-definition node :type))))))

(defn case-has-committed-data?
  {:test (fn []
           (is (case-has-committed-data? {:a {:committed true
                                              :data      "hejhopp"}
                                          :b {:committed true
                                              :data      "yoloswag"}}
                                         :a))
           (is (case-has-committed-data? {:a {:committed true
                                              :data      "hejhopp"}
                                          :b {:committed true
                                              :data      "yoloswag"}}
                                         :b))
           (is (false? (case-has-committed-data? {:a {:committed false
                                                      :data      "hejhopp"}
                                                  :b {:committed true
                                                      :data      "yoloswag"}}
                                                 :a)))
           (is (false? (case-has-committed-data? {:b {:committed true
                                                      :data      "yoloswag"}}
                                                 :a))))}
  [case data-key]
  (if (nil? (data-key case))
    false
    (:committed (data-key case))))

(defn data-prereqs-for-action
  {:test (fn []
           (is (= (data-prereqs-for-action (test-case-definition) :a) #{:b :e}))
           (is (= (data-prereqs-for-action (test-case-definition) :b) #{:e}))
           (is (= (data-prereqs-for-action (test-case-definition) :b) #{:e}))
           (is (= (data-prereqs-for-action (test-case-definition) :c) #{})))}
  [process-definition action]
  (->> (uber/find-edges process-definition {:src         action
                                            :association :requires})
       (map (fn [edge] (:dest edge)))
       (set)))

(defn data-produced-by-action
  {:test (fn []
           (is (= (data-produced-by-action (test-case-definition) :d) #{:a}))
           (is (= (data-produced-by-action (test-case-definition) :b) #{:c}))
           (is (= (data-produced-by-action (test-case-definition) :a) #{})))}
  [process-definition action]
  (->> (uber/find-edges process-definition {:src         action
                                            :association :produces})
       (map (fn [edge] (:dest edge)))
       (set)))

(defn actions-performed-by-role
  [process-definition role]
  (->> (uber/find-edges process-definition {:src         role
                                            :association :performs})
       (map (fn [edge] (:dest edge)))
       (set)))

(defn actions-with-prerequisites-present
  {:test (fn []
           (is (= (actions-with-prerequisites-present (test-case-definition) {}) #{:f}))
           (is (= (actions-with-prerequisites-present (test-case-definition) (add-data-to-case {} :e "yeah")) #{:d :f}))
           (is (= (actions-with-prerequisites-present (test-case-definition) (add-data-to-case {} :b "no")) #{:f}))
           (is (= (actions-with-prerequisites-present (test-case-definition) (-> {}
                                                                                 (add-data-to-case :e "yeah")
                                                                                 (add-data-to-case :b "no")))
                  #{:a :d :f})))}
  [process-definition case]
  (->> (all-actions process-definition)
       (reduce (fn [acc action]
                 (let [prereqs (data-prereqs-for-action process-definition action)]
                   (if (every? true? (map (fn [prereq] (case-has-committed-data? case prereq)) prereqs))
                     (conj acc action)
                     acc)))
               #{})))

(defn actions-performed
  {:test (fn []
           (is (= (actions-performed (test-case-definition) (add-data-to-case {} :a "swag")) #{:d}))
           (is (= (actions-performed (test-case-definition) (-> {}
                                                                (add-data-to-case :a "swag")
                                                                (add-data-to-case :c "yolo")))
                  #{:d :f}))
           (is (= (actions-performed (test-case-definition) (add-data-to-case {} :c "yolo")) #{:f}))
           (is (= (actions-performed (test-case-definition) {}) #{})))}
  [process-definition case]
  (->> (all-actions process-definition)
       (reduce (fn [acc action]
                 (let [produced-data (data-produced-by-action process-definition action)]
                   (cond
                     (empty? produced-data) acc
                     (every? true? (map (fn [data] (case-has-committed-data? case data)) produced-data)) (conj acc action)
                     :default acc)))
               #{})))

(defn next-actions
  ([process-definition case] (difference (actions-with-prerequisites-present process-definition case) (actions-performed process-definition case)))
  ([process-definition case role] (intersection (next-actions process-definition case) (actions-performed-by-role process-definition role))))

(defn action-allowed?
  {:test (fn []
           (is (false? (action-allowed? (test-case-definition) {} :a)))
           (is (false? (action-allowed? (test-case-definition) {:e "yeah"} :a)))
           (is (true? (action-allowed? (test-case-definition) (-> {}
                                                                  (add-data-to-case :b "no")
                                                                  (add-data-to-case :e "yeah")) :a)))
           (is (true? (action-allowed? (test-case-definition) (add-data-to-case {} :e "yeah") :d)))
           (is (false? (action-allowed? (test-case-definition) (add-data-to-case {} :c "yeah") :d)))
           (is (false? (action-allowed? (test-case-definition) (-> {}
                                                                   (add-data-to-case :a "lol")
                                                                   (add-data-to-case :b "yolo")
                                                                   (add-data-to-case :c "yeah")) :d))))}
  [process-definition case action]
  (contains? (actions-with-prerequisites-present process-definition case) action))
