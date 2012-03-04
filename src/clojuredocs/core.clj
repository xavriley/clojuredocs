(ns clojuredocs.core
  (:use [clojure.pprint :only (pprint pp)])
  (:require [clojure.string :as str]
            [somnium.congomongo :as mon]))

(defn pull-identity
  "Create a map of identity keys to values. To be used to make a query
  against the database based on a map's identity."
  [identity-keys m]
  (->> identity-keys
       (map #(% m))
       (interleave identity-keys)
       (apply hash-map)))

(defn clean
  "Non-persistable to persistable data types."
  [val]
  (cond
   (vector? val) (vec (map clean val))
   (coll? val) (map clean val)
   (instance? java.lang.Class val) (.getName val)
   (instance? clojure.lang.Namespace val) (str val)
   (instance? clojure.lang.Symbol val) (str val)
   :else val))

(defn clean-map
  "Turns values of a map into persitable data types."
  [m]
  (->> m (map (fn [e] [(first e) (clean (second e))])) (into {})))

(def ClojureVar
  {:identity [:ns :name :version]
   :collection :clojure_vars})

(defn persist [meta omap]
  (let [cleaned-map (clean-map omap)
        ident (:identity meta)
        ident-query (pull-identity ident cleaned-map)
        collection (:collection meta)]
    (mon/fetch-and-modify collection
                          ident-query
                          cleaned-map
                          :upsert? true
                          :return-new? true)))

#_(count (mon/fetch (:collection ClojureVar)))

#_(mon/mongo! :db :clojuredocs)

#_(do
  (mon/drop-coll! (:collection ClojureVar))
  (count (mon/fetch (:collection ClojureVar))))


#_(defn var-persist
  "Persist vars"
  [ident v]
  )

#_(def cd-var {:ns (find-ns 'clojure.string)
             :name 'trim,
             :arglists '([s]),
             :added "1.2",
             :doc "Removes whitespace from both ends of string.",
             :line 184,
             :file "clojure/string.clj",
             :tag java.lang.String
             :version "1.3.0"})

#_(def var-identity [:version :name :ns])
#_(def var-coll :vars)

#_(->> 'clojure.string
     ns-publics
     (map second)
     (map meta)
     (map #(var-persist [:version :name :ns] %))
     pprint)




