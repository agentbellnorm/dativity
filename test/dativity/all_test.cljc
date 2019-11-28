(ns dativity.all-test
  (:require [clojure.test :refer :all]
            [dativity.core]
            [dativity.define]
            [dativity.graph]
            [dativity.visualize]
            [dativity.core-test]
            [dativity.define-test]))

(clojure.test/run-all-tests #"dativity.*")
