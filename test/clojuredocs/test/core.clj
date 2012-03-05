(ns clojuredocs.test.core
  (:use clojuredocs.core :reload)
  (:use [clojure.test])
  (:require [somnium.congomongo  :as mon]))

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


(mon/mongo! :db :clojuredocs-test)

(defn drop-colls [& colls]
  (doseq [coll colls]
    (mon/drop-coll! coll)))

(deftest test-persist-ns
  (drop-colls (:collection ClojureNamespace))
  (let [ns1 (persist-ns (find-ns 'clojure.string) "lib" "ver" "hash")
        ns2 (persist-ns (find-ns 'clojure.string) "lib" "ver" "hash")]
    (is (= ns1 ns2))))

(deftest test-persist-var
  (drop-colls (:collection ClojureVar))
  (let [var1 (persist-var #'take "lib" "ver" "hash")
        var2 (persist-var #'take "lib" "ver" "hash")]
    (is (= var1 var2))))
