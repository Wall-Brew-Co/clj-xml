(ns clj-xml.impl
  "The underlying functions that implement the public interface"
  (:require [clojure.string :as cs]))

(defn xml-tag->keyword
  "Take an XML tag as extracted by `clojure.data.xml` and turn it into a kebab-cased, lower case keyword"
  [xml-tag]
  (let [xml-str (cs/lower-case (name xml-tag))]
    (keyword (cs/replace xml-str "_" "-"))))

(defn keyword->xml-tag
  "Take a clojure keyord and turn it into the form expected by `clojure.data.xml` by making it UPPER CASE and snake_cased"
  [edn-keyword]
  (let [edn-str (cs/upper-case (name edn-keyword))]
    (keyword (cs/replace edn-str "-" "_"))))

(def ^:const attrs-length
  (count "-attrs"))

(defn attrs-tag->tag
  "Remove a suffix of `-attrs/_ATTRS` from `attrs-tag`"
  [attrs-tag]
  (let [tag-length (count attrs-tag)]
    (subs attrs-tag 0 (- tag-length attrs-length))))

(defn tag->attrs-tag
  "Transform `tag` to look like an attributes map tag by appending it with `-attrs/_ATTRS` pedending on the value of `upper-case?`"
  [tag upper-case?]
  (let [suffix (if upper-case? "_ATTRS" "-attrs")]
    (keyword (str (name tag) suffix))))

(defn edn-attrs-tag?
  "Returns true iff the list of `all-tags` to see if it contains the normalized `tag`"
  [tag all-tags]
  (boolean (and (cs/ends-with? (cs/lower-case tag) "attrs")
                (contains? all-tags (attrs-tag->tag tag)))))

(defn unique-tags?
  "Take an XML sequence as formatted by `clojure.xml/parse`, and determine if it exclusively contains unique tags"
  [xml-seq]
  (let [unique-tag-count (count (distinct (keep :tag xml-seq)))
        tag-count        (count (map :tag xml-seq))]
    (= unique-tag-count tag-count)))

(defn deformat
  "Remove line termination formatting specific to Windows (since we're ingesting XML) and double spacing"
  [s {:keys [remove-newlines?]}]
  (cond-> s
    :always          (cs/replace #"\r\n" "")
    :always          (cs/replace #"\s\s+" "")
    remove-newlines? (cs/replace #"\n" "")))

(defn update-vals
  "Return `m` with `f` applied to each val in `m` with its `args`"
  [m f & args]
  (reduce-kv (fn [m' k v] (assoc m' k (apply f v args))) {} m))

(defn update-keys
  "Return `m` with `f` applied to each key in `m` with its `args`"
  [m f & args]
  (reduce-kv (fn [m' k v] (assoc m' (apply f k args) v)) {} m))
