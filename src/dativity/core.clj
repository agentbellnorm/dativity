(ns dativity.core
  (:require [dativity.define :as define]
            [ubergraph.core :as uber]
            [clojure.test :refer :all]
            [clojure.set :refer :all]))

(defn printreturn [x] (println x) x)

(defn test-case-definition
  "test graph for unit testing purposes, does not make sense really, but is simple."
  []
  (-> (define/empty-case-model)
      (define/add-entity-to-model (define/action :a))
      (define/add-entity-to-model (define/action :d))
      (define/add-entity-to-model (define/action :f))
      (define/add-entity-to-model (define/action :g))
      (define/add-entity-to-model (define/action :i))
      (define/add-entity-to-model (define/data :j))
      (define/add-entity-to-model (define/role :b))         ; doesn't make sense but needs to be some kind of entity
      (define/add-entity-to-model (define/role :c))
      (define/add-entity-to-model (define/role :e))
      (define/add-entity-to-model (define/role :h))
      (define/add-relationship-to-model (define/action-requires :a :b))
      (define/add-relationship-to-model (define/action-requires :a :e))
      (define/add-relationship-to-model (define/action-requires :b :e))
      (define/add-relationship-to-model (define/action-requires :d :e))
      (define/add-relationship-to-model (define/action-requires :i :b))
      (define/add-relationship-to-model (define/action-requires-conditional :i :j (fn [b-data] (= (:elite b-data) 1337)) :b))
      (define/add-relationship-to-model (define/action-produces :b :c))
      (define/add-relationship-to-model (define/action-produces :d :a))
      (define/add-relationship-to-model (define/action-produces :f :c))
      (define/add-relationship-to-model (define/action-produces :g :e))
      (define/add-relationship-to-model (define/action-produces :g :h))
      (define/add-relationship-to-model (define/role-performs :a :c))
      (define/add-relationship-to-model (define/role-performs :c :d))))

(comment (uber/viz-graph (test-case-definition)))

(defn add-data-to-case                                      ;; TODO: Should subsequent actions be invalidated if the data was already there?
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
           (is (= (all-actions (test-case-definition)) #{:a :d :f :g :i})))}
  [process-definition]
  (->> (uber/nodes process-definition)
       (filter (fn [node] (= :action (uber/attr process-definition node :type))))
       (set)))

(defn case-has-data?
  "Returns true if the given data node exists regardless if it is committed or not."
  {:test (fn []
           (is (case-has-data? {:a {:committed true :value "hejhopp"}} :a))
           (is (case-has-data? {:a {:committed false :value "hejhopp"}} :a))
           (is (case-has-data? {:a {:committed false :value nil}} :a))
           (is (not (case-has-data? {} :a)))
           )}
  [case data-key]
  (not (nil? (data-key case))))

(defn case-has-committed-data?
  "Returns true if the given data node exists and is committed"
  {:test (fn []
           (is (case-has-committed-data? {:a {:committed true
                                              :value     "hejhopp"}
                                          :b {:committed true
                                              :value     "yoloswag"}}
                                         :a))
           (is (case-has-committed-data? {:a {:committed true
                                              :value     "hejhopp"}
                                          :b {:committed true
                                              :value     "yoloswag"}}
                                         :b))
           (is (false? (case-has-committed-data? {:a {:committed false
                                                      :value     "hejhopp"}
                                                  :b {:committed true
                                                      :value     "yoloswag"}}
                                                 :a)))
           (is (false? (case-has-committed-data? {:b {:committed true
                                                      :value     "yoloswag"}}
                                                 :a))))}
  [case data-key]
  (if (nil? (data-key case))
    false
    (:committed (data-key case))))

(defn case-has-uncommitted-data?
  "Returns true if the given data node exists on the case and is uncommitted"
  {:test (fn []
           (is (case-has-uncommitted-data? {:a {:committed false
                                                :value     "dank"}} :a))
           (is (not (case-has-uncommitted-data? {:a {:committed true
                                                     :value     "yoloswaggins"}} :a)))
           (is (not (case-has-uncommitted-data? {} :a)))
           )}
  [case data-key]
  (and (case-has-data? case data-key) (not (case-has-committed-data? case data-key))))

(defn data-prereqs-for-action
  {:test (fn []
           (is (= (data-prereqs-for-action {} (test-case-definition) :a) #{:b :e}))
           (is (= (data-prereqs-for-action {} (test-case-definition) :b) #{:e}))
           (is (= (data-prereqs-for-action {} (test-case-definition) :b) #{:e}))
           (is (= (data-prereqs-for-action {} (test-case-definition) :c) #{}))
           (is (= (data-prereqs-for-action {} (test-case-definition) :i) #{}))
           (is (data-prereqs-for-action (add-data-to-case {} :))))} ;; TODO : fortsätt här, den ska kontrollera vilkor baserat på datan och filtrera prereqs efter det.
  [process-definition case action]
  (->> (uber/find-edges process-definition {:src         action
                                            :association :requires})
       (concat (uber/find-edges process-definition {:src         action
                                                    :association :requires-conditional}))
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

(defn actions-that-require-data
  {:test (fn []
           (is (= (actions-that-require-data (test-case-definition) :e) #{:b :d :a}))
           (is (= (actions-that-require-data (test-case-definition) :b) #{:a})))}
  [process-definition data]
  (->> (uber/find-edges process-definition {:dest        data
                                            :association :requires})
       (map (fn [edge] (:src edge)))
       (set)))

(defn- actions-that-can-be-performed-after-action
  {:test (fn []
           (is (= (actions-that-can-be-performed-after-action (test-case-definition) :g) #{:a :b :d})))}
  [process-definition action]
  (apply union (map (fn [data] (actions-that-require-data process-definition data))
                    (data-produced-by-action process-definition action))))

(defn uncommit-data
  {:test (fn []
           (is (false? (-> (add-data-to-case {} :a "swell")
                           (uncommit-data :a)
                           (case-has-committed-data? :a))))
           (is (true? (-> (add-data-to-case {} :a "swell")
                          (add-data-to-case :b "turnt")
                          (uncommit-data :a)
                          (case-has-committed-data? :b))))
           (is (= (uncommit-data {} :a) {})))}
  [case key]
  (if (contains? case key)
    (update-in case [key] assoc :committed false)
    case))

(defn actions-with-prereqs-present
  {:test (fn []
           (is (= (actions-with-prereqs-present (test-case-definition) {}) #{:f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (add-data-to-case {} :e "yeah")) #{:d :f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (add-data-to-case {} :b "no")) #{:f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :e "yeah")
                                                                           (add-data-to-case :b "no")))
                  #{:a :d :f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (add-data-to-case {} :e "total")) #{:d :f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :e "total")
                                                                           (uncommit-data :e)))
                  #{:f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :b {:elite 1337})
                                                                           (add-data-to-case :j "radical")))
                  #{:i :f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :b {:elite 1337})
                                                                           (add-data-to-case :j "radical")
                                                                           (uncommit-data :j)))
                  #{:f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :b {:elite 1337})))
                  #{:f :g}))
           (is (= (actions-with-prereqs-present (test-case-definition) (-> {}
                                                                           (add-data-to-case :b {:elite 1336})))
                  #{:i :f :g})))}
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
  ([process-definition case] (difference (actions-with-prereqs-present process-definition case) (actions-performed process-definition case)))
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
  (contains? (actions-with-prereqs-present process-definition case) action))

(defn- uncommit-datas-produced-by-action
  "uncommits all data nodes that have a production edge from the specified action node"
  {:test (fn []
           (is (true? (as-> {} case
                            (add-data-to-case case :e "ey")
                            (add-data-to-case case :h "kolasås")
                            (add-data-to-case case :a "sås")
                            (uncommit-datas-produced-by-action (test-case-definition) case :g)
                            (every? false? [(case-has-committed-data? case :e)
                                            (case-has-committed-data? case :h)])))))}
  [process-definition case action]
  (loop [loop-case case
         [data & datas] (data-produced-by-action process-definition action)]
    (if (nil? data)
      loop-case
      (recur (uncommit-data loop-case data)
             datas))))

(defn invalidate-action
  "Uncommits the data produced by the specified action, and then recursively performs
  the same procedure on all actions that require the data produced by the specified action."
  {:test (fn []
           (as-> {} case
                 (add-data-to-case case :c "far out")
                 (add-data-to-case case :h "no way")
                 (invalidate-action (test-case-definition) case :b)
                 (do
                   (is (not (case-has-committed-data? case :c)))
                   (is (case-has-committed-data? case :h))
                   (is (not (action-allowed? (test-case-definition) case :b)))))
           (as-> {} case
                 (add-data-to-case case :c "far out")
                 (add-data-to-case case :e "yoloswaggins")
                 (add-data-to-case case :a "dope")
                 (add-data-to-case case :h "fuego")
                 (invalidate-action (test-case-definition) case :g)
                 (do
                   (is (false? (case-has-committed-data? case :c)))
                   (is (false? (case-has-committed-data? case :e)))
                   (is (false? (case-has-committed-data? case :a)))
                   (is (false? (case-has-committed-data? case :h)))))
           (as-> {} case
                 (add-data-to-case case :c "far out")
                 (add-data-to-case case :e "yoloswaggins")
                 (add-data-to-case case :a "dope")
                 (add-data-to-case case :h "fuego")
                 (invalidate-action (test-case-definition) case :d)
                 (is (not-any? false? [(case-has-committed-data? case :h)
                                       (case-has-committed-data? case :e)]))
                 (is (not-any? true? [(case-has-committed-data? case :c)
                                      (case-has-committed-data? case :a)]))))}
  [process-definition case action]
  (loop [loop-case case
         [loop-action & loop-actions] [action]
         seen-actions #{}]
    (if (nil? loop-action)
      loop-case
      (recur (uncommit-datas-produced-by-action process-definition loop-case loop-action)
             (difference (set (concat (actions-that-can-be-performed-after-action process-definition loop-actions) loop-actions)) seen-actions)
             (conj seen-actions loop-action)))))

