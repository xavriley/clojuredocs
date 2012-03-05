(ns clojuredocs.core
  (:use [clojure.pprint :only (pprint)])
  (:require [clojure.string :as str]
            [somnium.congomongo :as mon])
  (:import [java.io PushbackReader]))

;; # Peristence

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

(defn git-url [{:keys [library commit-hash file src-root line]}]
  (str "https://github.com"
       "/" library
       "/blob"
       "/" commit-hash
       "/" src-root
       "/" file
       "#L" line))


;; # Namespaces

(def ^{:doc "Persistence metadata for namespaces."}
  ClojureNamespace
  {:identity [:name :version :library]
   :collection :namespaces})

(defn persist-ns [ns library version commit-hash]
  (->> {:name (.getName ns)
        :version version
        :library library
        :commit-hash commit-hash}
       (merge (meta ns))
       clean-map
       (persist ClojureNamespace)))


;; # Vars

(def ^{:doc "Persistence metadata for vars."}
  ClojureVar
  {:identity [:ns :name :version :library]
   :collection :vars})

(defn persist-var [var library version commit-hash]
  (->> (meta var)
       (merge {:library library
               :version version
               :commit-hash commit-hash})
       clean-map
       (persist ClojureNamespace)))

;; # Files

;; From clojure.contrib.find-namespaces

(defn comment?
  "Returns true if form is a (comment ...)"
  [form]
    (and (list? form) (= 'comment (first form))))

(defn ns-decl?
  "Returns true if form is a (ns ...) declaration."
  [form]
  (and (list? form) (= 'ns (first form))))

(defn read-ns-decl
  "Attempts to read a (ns ...) declaration from rdr, and returns the
  unevaluated form.  Returns nil if read fails or if a ns declaration
  cannot be found.  The ns declaration must be the first Clojure form
  in the file, except for (comment ...)  forms."
  [^PushbackReader rdr]
  (try
    (loop [rdr rdr]
      (let [form (read rdr)]
        (cond
         (ns-decl? form) form
         (comment? form) (recur rdr)
         :else nil)))
    (catch Exception e nil)))


;;;

(comment

  (with-open [rdr (clojure.lang.LineNumberingPushbackReader. (java.io.FileReader. "./src/clojuredocs/core.clj"))]
    (read-ns-decl rdr))
  
  (mon/mongo! :db :clojuredocs)
  (pprint (->> 'clojure.string
               ns-publics
               (map second)
               (map meta)
               (map #(assoc %
                       :library "clojure/clojure"
                       :version "1.3.0"
                       :commit-hash "1f55cc0a9df8e98c79973a1f563bb68baab7bccd"
                       :src-root "src/clj"))
               (map #(persist ClojureVar %))
               (map git-url)))


  (mon/fetch (:collection ClojureVar) :where {:library "clojure/clojure"})

  (count (mon/fetch (:collection ClojureVar)))

  (mon/mongo! :db :clojuredocs)

  (do
    (mon/drop-coll! (:collection ClojureVar))
    (count (mon/fetch (:collection ClojureVar)))))



