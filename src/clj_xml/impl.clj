(ns ^:no-doc clj-xml.impl
  "The underlying functions that implement the public interface.

   Functions in this namespace are not intended to be used directly by consumers of this library."
  {:no-doc true}
  (:require [clojure.string :as str]))


(defn xml-tag->keyword
  "Take an XML tag as extracted by `clojure.data.xml` and turn it into a kebab-cased, lower case keyword.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [xml-tag]
  (let [xml-str (str/lower-case (name xml-tag))]
    (keyword (str/replace xml-str "_" "-"))))


(defn keyword->xml-tag
  "Take a clojure keyord and turn it into the form expected by `clojure.data.xml` by making it UPPER CASE and snake_cased.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [edn-keyword]
  (let [edn-str (str/upper-case (name edn-keyword))]
    (keyword (str/replace edn-str "-" "_"))))


(def ^:const attrs-length
  "The length of the suffix that is appended to a tag to make it look like an attributes map tag."
  (count "-attrs"))


(defn attrs-tag->tag
  "Remove a suffix of `-attrs/_ATTRS` from `attrs-tag`.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [attrs-tag]
  (let [tag-length (count attrs-tag)]
    (subs attrs-tag 0 (- tag-length attrs-length))))


(defn tag->attrs-tag
  "Transform `tag` to look like an attributes map tag by appending it with `-attrs/_ATTRS` pedending on the value of `upper-case?`.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [tag upper-case?]
  (let [suffix (if upper-case? "_ATTRS" "-attrs")]
    (keyword (str (name tag) suffix))))


(defn edn-attrs-tag?
  "Returns true iff the list of `all-tags` to see if it contains the normalized `tag`.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [tag all-tags]
  (boolean (and (str/ends-with? (str/lower-case tag) "attrs")
                (contains? all-tags (attrs-tag->tag tag)))))


(defn unique-tags?
  "Take an XML sequence as formatted by `clojure.xml/parse`, and determine if it exclusively contains unique tags.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [xml-sequence]
  (let [unique-tag-count (count (distinct (keep :tag xml-sequence)))
        tag-count        (count (map :tag xml-sequence))]
    (= unique-tag-count tag-count)))


(defn deformat
  "Remove line termination formatting specific to Windows (since we're ingesting XML) and double spacing.

   Not intended to be consumed outside of this library."
  {:added  "1.0"
   :no-doc true}
  [s {:keys [remove-newlines?]}]
  (let [trimmed-str (if remove-newlines?
                      (apply str (str/split-lines s))
                      (str/replace s #"\r\n" "\n"))]
    (str/replace trimmed-str #"  " " ")))


(defn update-vals*
  "Return `m` with `f` applied to each val in `m` with its `args`.

   Used to maintain backwards-compatibility for consumers on older versions of clojure.
   Not intended to be consumed outside of this library."
  {:added   "1.0"
   :changed "1.7"
   :no-doc  true}
  [m f & args]
  (reduce-kv (fn [m' k v] (assoc m' k (apply f v args))) {} m))


(defn update-keys*
  "Return `m` with `f` applied to each key in `m` with its `args`.

   Used to maintain backwards-compatibility for consumers on older versions of clojure.
   Not intended to be consumed outside of this library."
  {:added   "1.0"
   :changed "1.7"
   :no-doc  true}
  [m f & args]
  (reduce-kv (fn [m' k v] (assoc m' (apply f k args) v)) {} m))
