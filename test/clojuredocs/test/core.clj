(ns clojuredocs.test.core
  (:use clojuredocs.core :reload)
  (:use [clojure.test]))

(deftest test-clean
  (are [exp type] (= exp (clean type))
       "foo"              'foo
       "java.lang.String" java.lang.String
       "clojure.string"   (find-ns 'clojure.string)
       ["foo"]            ['foo])
  
  (is  (vector? (clean ['foo])))
  (is  (seq?    (clean '(foo)))))

(deftest test-clean-map
  (= "foo" (-> {:foo 'foo} clean-map :foo)))



