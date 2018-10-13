(ns dativity.core
  (:require [ubergraph.core :as uber]
            [clojure.test :refer :all]
            [clojure.set :refer :all]))

(defn printreturn [x] (println x) x)

(defn test-case-definition
  []
  (-> (uber/multigraph [:a {:type :action}] :b :c [:d {:type :action}] :e [:f {:type :action}])
      (uber/add-directed-edges [:a :b {:association :requires
                                       :color       :red}]
                               [:a :c {:association :performs
                                       :color       :orange}]
                               [:a :e {:association :requires
                                       :color       :red}]
                               [:b :c {:association :produces
                                       :color       :green}]
                               [:b :e {:association :requires
                                       :color       :red}]
                               [:c :d {:association :performs
                                       :color       :orange}]
                               [:d :e {:association :requires
                                       :color       :red}]
                               [:d :a {:association :produces
                                       :color       :green}]
                               [:f :c {:association :produces
                                       :color       :green}])))

(comment (uber/viz-graph (test-case-definition)))

(defn all-actions
  {:test (fn []
           (is (= (all-actions (test-case-definition)) [:a :d :f])))}
  [process-definition]
  (->> (uber/nodes process-definition)
       (filter (fn [node] (= :action (uber/attr process-definition node :type))))))

(defn case-has-commited-data?
  {:test (fn []
           (is (case-has-commited-data? {:a {:commited true
                                             :data "hejhopp"}
                                         :b {:commited true
                                             :data "yoloswag"}}
                                        :a))
           (is (case-has-commited-data? {:a {:commited true
                                             :data "hejhopp"}
                                         :b {:commited true
                                             :data "yoloswag"}}
                                        :b))
           (is (false? (case-has-commited-data? {:a {:commited false
                                                     :data "hejhopp"}
                                                 :b {:commited true
                                                     :data "yoloswag"}}
                                                :a)))
           (is (false? (case-has-commited-data? {:b {:commited true
                                                     :data "yoloswag"}}
                                                :a))))}
  [case data-object]
  (if (nil? (data-object case))
    false
    (:commited (data-object case))))

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
           (is (= (actions-with-prerequisites-present (test-case-definition) {:e "yeah"}) #{:d :f}))
           (is (= (actions-with-prerequisites-present (test-case-definition) {:b "no"}) #{:f}))
           (is (= (actions-with-prerequisites-present (test-case-definition) {:e "yeah" :b "no"}) #{:a :d :f})))}
  [process-definition case]
  (->> (all-actions process-definition)
       (reduce (fn [acc action]
                 (let [prereqs (data-prereqs-for-action process-definition action)]
                   (if (every? true? (map (fn [prereq] (case-has-commited-data? case prereq)) prereqs))
                     (conj acc action)
                     acc)))
               #{})))

(defn actions-performed
  {:test (fn []
           (is (= (actions-performed (test-case-definition) {:a "swag"}) #{:d}))
           (is (= (actions-performed (test-case-definition) {:a "swag" :c "yolo"}) #{:d :f}))
           (is (= (actions-performed (test-case-definition) {:c "yolo"}) #{:f}))
           (is (= (actions-performed (test-case-definition) {}) #{})))}
  [process-definition case]
  (->> (all-actions process-definition)
       (reduce (fn [acc action]
                 (let [produced-data (data-produced-by-action process-definition action)]
                   (cond
                     (empty? produced-data) acc
                     (every? true? (map (fn [data] (case-has-commited-data? case data)) produced-data)) (conj acc action)
                     :default acc)))
               #{})))

(defn next-actions
  ([process-definition case] (difference (actions-with-prerequisites-present process-definition case) (actions-performed process-definition case)))
  ([process-definition case role] (intersection (next-actions process-definition case) (actions-performed-by-role process-definition role))))

(defn action-allowed?
  {:test (fn []
           (is (false? (action-allowed? (test-case-definition) {} :a)))
           (is (false? (action-allowed? (test-case-definition) {:e "yeah"} :a)))
           (is (true? (action-allowed? (test-case-definition) {:b "no" :e "yeah"} :a)))
           (is (true? (action-allowed? (test-case-definition) {:e "yeah"} :d)))
           (is (false? (action-allowed? (test-case-definition) {:c "yeah"} :d)))
           (is (false? (action-allowed? (test-case-definition) {:a "lol" :b "yolo" :c "yeah"} :d))))}
  [process-definition case action]
  (contains? (actions-with-prerequisites-present process-definition case) action))
