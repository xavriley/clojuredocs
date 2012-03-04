(ns clojuredocs.test.performance)

(defn nanos []
  (System/nanoTime))

(defmacro wall-time [& forms]
  `(let [st# (nanos)
         ret# (do ~@forms)]
     [(- (nanos) st#) ret#]))

(defn average [vals]
  (double (/ (reduce + vals) (count vals))))

(->> (range 10000)
     (map #(wall-time (str % "foo")))
     (map first)
     average
     println)


