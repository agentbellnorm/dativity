(ns dativity.all
  (:require [clojure.test :refer :all]
            [dativity.core]
            [dativity.define]
            [dativity.visualize]
            [dativity.graph]
            [dativity.core-test]
            [dativity.define-test]))

(run-all-tests)
