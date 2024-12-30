(ns clj-xml.core
  "A bunch of utility functions for xml documents."
  (:require [clj-xml.impl :as impl]
            [clojure.data.xml :as xml])
  (:import [java.lang IllegalArgumentException]))


(set! *warn-on-reflection* true)


(def every-child
  "An alias for the ::every namespaced keyword.

   Used to ensure selective paths in the returned XML are coerced to sequences.
   This option forces all child nodes to be coerced into a sequence of maps."
  ::every)


(def first-child
  "An alias for the ::first namespaced keyword.

   Used to ensure selective paths in the returned XML are coerced to sequences.
   This option forces only the first child node to be coerced into a sequence of maps."
  ::first)


(def last-child
  "An alias for the ::last namespaced keyword.

   Used to ensure selective paths in the returned XML are coerced to sequences.
   This option forces only the last child node to be coerced into a sequence of maps."
  ::last)


(def ^:private child-keys
  "The set of all allowable child keys used by `force-xml-seq-at-path`"
  #{first-child last-child every-child})


(defn- child-key?
  "Returns true iff `k` is in the set of child keys."
  [k]
  (contains? child-keys k))


(defn force-xml-seq-at-path
  "Update `xml-edn` to convert the specified child node in the key path to a vector.
   `key-paths` is a sequence of `key` and `key-seq`, each of which is either a bare keywords or the following namespaced keywords:
      - ::every or the alias every-child
      - ::first or the alias first-child
      - ::last or the alias last-child
    For each element in `key-path`, `force-xml-seq-at-path` will traverse `xml-edn` one level
       - If the current node is a map, clj-xml expects a keyword to update
       - If the current node is a sequence, clj-xml expects one of the supplied namespaced keywords and will update the related members of that sequence"
  {:added    "1.6"
   :see-also ["every-child" "first-child" "last-child"]}
  [xml-edn [xml-key & key-seq]]
  (if xml-key
    (cond
      (and (sequential? xml-edn)
           (= every-child xml-key))   (mapv #(force-xml-seq-at-path % key-seq) xml-edn)
      (and (sequential? xml-edn)
           (= first-child xml-key))   (cons (force-xml-seq-at-path (first xml-edn) key-seq) (rest xml-edn))
      (and (sequential? xml-edn)
           (= last-child xml-key))    (conj (into [] (butlast xml-edn)) (force-xml-seq-at-path (last xml-edn) key-seq))
      (and (map? xml-edn)
           (not (child-key? xml-key))
           (keyword? xml-key))        (update xml-edn xml-key force-xml-seq-at-path key-seq)
      :else                           (throw (IllegalArgumentException. (str "The key " xml-key " is incompatible with " (type xml-edn)))))
    [xml-edn]))


(defn force-xml-seq-at-paths
  "Convert every path specified in `key-paths` within `xml-edn` to convert the specified child node to a vector.
   `key-paths` is a sequence of sequences, each of which contains a mixture of bare keywords or the following namespaced keywords:
      - ::every or the alias every-child
      - ::first or the alias first-child
      - ::last or the alias last-child
    For each element in `key-path`, `force-xml-seq-at-paths` will traverse `xml-edn` one level
       - If the current node is a map, clj-xml expects a keyword to update
       - If the current node is a sequence, clj-xml expects one of the supplied namespaced keywords and will update the related members of that sequence"
  {:added    "1.6"
   :see-also ["every-child" "first-child" "last-child"]}
  [xml-edn key-paths]
  (reduce force-xml-seq-at-path xml-edn key-paths))


;; Parsing XML into EDN

(declare xml->edn)


(defn xml-seq->edn
  "Transform an XML sequence as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map be provided with the following keys:
     preserve-keys?      - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - to maintain embedded XML attributes
     stringify-values?   - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps
     force-seq?          - to coerce child XML nodes into a sequence of maps"
  {:added "1.0"}
  ([xml-sequence]
   (xml-seq->edn xml-sequence {}))

  ([xml-sequence {:keys [stringify-values? force-seq?] :as opts}]
   (cond
     (impl/string-or-nil? xml-sequence)
     xml-sequence

     (and (coll? xml-sequence)
          (= 1 (count xml-sequence))
          (impl/string-or-nil? (first xml-sequence)))
     (first xml-sequence)

     (and (not force-seq?)
          (coll? xml-sequence)
          (> (count xml-sequence) 1)
          (impl/unique-tags? xml-sequence))
     (reduce (fn [acc v] (merge acc (xml->edn v opts))) {} xml-sequence)

     (and (map? xml-sequence)
          (empty? xml-sequence))
     {}

     (map? xml-sequence)
     (xml->edn xml-sequence opts)

     (sequential? xml-sequence)
     (mapv #(xml->edn % opts) xml-sequence)

     (and stringify-values?
          (some? xml-sequence))
     (str xml-sequence)

     :else xml-sequence)))


(defn xml-map->edn
  "Transform an XML map as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map be provided with the following keys:
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps"
  {:added "1.0"}
  ([xml-map]
   (xml-map->edn xml-map {}))

  ([{:keys [tag attrs content]} {:keys [preserve-keys? preserve-attrs? stringify-values? remove-empty-attrs?]
                                 :as   opts}]
   (let [edn-tag   (impl/keywordify tag preserve-keys?)
         edn-value (xml->edn content opts)]
     (if (and attrs preserve-attrs?)
       (let [attrs-key  (impl/tag->attrs-tag edn-tag preserve-keys?)
             add-attrs? (or (not remove-empty-attrs?)
                            (and remove-empty-attrs?
                                 (seq attrs)))]
         (merge {edn-tag edn-value}
                (when add-attrs? {attrs-key (reduce-kv (fn [m k v]
                                                         (assoc m
                                                                (impl/keywordify k preserve-keys?)
                                                                (impl/stringify v stringify-values?)))
                                                       {}
                                                       attrs)})))
       {edn-tag edn-value}))))


(defn xml->edn
  "Transform an XML document as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.
   To change this behavior, an option map may be provided with the following keys:
     preserve-keys?      - A boolean, that if set to true, maintains the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - A boolean, that if set to true, maintains embedded XML attributes
     stringify-values?   - A boolean, that if set to true, coerces non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - A boolean, that if set to true, removes any empty attribute maps
     force-seq?          - A boolean, that if set to true, coerces child XML nodes into a sequence of maps"
  {:added "1.0"}
  ([xml-doc]
   (xml->edn xml-doc {}))

  ([xml-doc {:keys [stringify-values?] :as opts}]
   (cond
     (impl/string-or-nil? xml-doc)
     xml-doc

     (sequential? xml-doc)
     (xml-seq->edn xml-doc opts)

     (and (map? xml-doc)
          (empty? xml-doc))
     {}

     (map? xml-doc)
     (xml-map->edn xml-doc opts)

     (and stringify-values?
          (some? xml-doc))
     (str xml-doc))))


(defn xml->edn'
  "Wrapper around xml->edn to apply sequence coercion"
  {:added "1.0"}
  ([xml-doc]
   (xml->edn' xml-doc {}))

  ([xml-doc {:keys [force-seq-for-paths] :as opts}]
   (cond-> xml-doc
     :always                   (xml->edn opts)
     (seq force-seq-for-paths) (force-xml-seq-at-paths force-seq-for-paths))))


(defn xml-str->edn
  "Parse an XML document with `clojure.xml/parse-str` and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map may be provided with the following keys:
     preserve-keys?      - A boolean, that if set to true, maintains the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - A boolean, that if set to true, maintains embedded XML attributes
     stringify-values?   - A boolean, that if set to true, coerces non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - A boolean, that if set to true, removes any empty attribute maps
     remove-newlines?    - A boolean, that if set to true, removes any newline characters in `xml-str`
     force-seq?          - A boolean, that if set to true, coerces child XML nodes into a sequence of maps
     force-seq-for-paths - A sequence of key-path sequences that will be coerced into sequences.

   It also surfaces the original options from `clojure.data.xml/parse-str`
     include-node?                - A set containing elements of #{:element :characters :comment} default #{:element :characters}
     location-info                - A boolean, that if set to false, skips generating location meta data
     allocator                    - An instance of a XMLInputFactory/ALLOCATOR to allocate events
     coalescing                   - A boolean, that if set to true, coalesces adjacent characters
     namespace-aware              - A boolean, that if set to false, disables XML 1.0 namespacing support
     replacing-entity-references  - A boolean, that if set to false, disables entity text replacement
     supporting-external-entities - A boolean, that if set to true, will resolve external entities and parse them
     validating                   - A boolean, that if set to true, will enable DTD validation
     reporter                     - An instance of a XMLInputFactory/REPORTER to use in place of defaults
     resolver                     - An instance of a XMLInputFactory/RESOLVER to use in place of defaults
     support-dtd                  - A boolean, that if set to false, disables DTD support in parsers
     skip-whitespace              - A boolean, that if set to true, removes whitespace only elements"
  {:added "1.0"}
  ([xml-str]
   (xml-str->edn xml-str {}))

  ([xml-str opts]
   (let [additional-args (select-keys opts [:include-node?
                                            :location-info
                                            :allocator
                                            :coalescing
                                            :namespace-aware
                                            :replacing-entity-references
                                            :supporting-external-entities
                                            :validating
                                            :reporter
                                            :resolver
                                            :support-dtd
                                            :skip-whitespace])
         flattened-args  (flatten (into [] additional-args))
         sanitized-xml   (impl/deformat xml-str opts)
         parsing-args    (cons sanitized-xml flattened-args)
         parsed-xml      (apply xml/parse-str parsing-args)]
     (xml->edn' parsed-xml opts))))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}


(defn xml-source->edn
  "Parse an XML document source with `clojure.xml/parse` and transform it into normalized EDN.
   `xml-source` may be an instance of java.io.InputStream or java.io.Reader
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map may be provided with the following keys:
     preserve-keys?      - A boolean, that if set to true, maintains the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - A boolean, that if set to true, maintains embedded XML attributes
     stringify-values?   - A boolean, that if set to true, coerces non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - A boolean, that if set to true, removes any empty attribute maps
     force-seq?          - A boolean, that if set to true, coerces child XML nodes into a sequence of maps
     force-seq-for-paths - A sequence of key-path sequences that will be coerced into sequences.

   It also surfaces the original options from `clojure.data.xml/parse`
     include-node?                - a subset of #{:element :characters :comment} default #{:element :characters}
     location-info                - pass false to skip generating location meta data
     allocator                    - An instance of a XMLInputFactory/ALLOCATOR to allocate events
     coalescing                   - A boolean, that if set to true, coalesces adjacent characters
     namespace-aware              - A boolean, that if set to false, disables XML 1.0 namespacing support
     replacing-entity-references  - A boolean, that if set to false, disables entity text replacement
     supporting-external-entities - A boolean, that if set to true, will resolve external entities and parse them
     validating                   - A boolean, that if set to true, will enable DTD validation
     reporter                     - An instance of a XMLInputFactory/REPORTER to use in place of defaults
     resolver                     - An instance of a XMLInputFactory/RESOLVER to use in place of defaults
     support-dtd                  - A boolean, that if set to false, disables DTD support in parsers
     skip-whitespace              - A boolean, that if set to true, removes whitespace only elements"
  {:added "1.0"}
  ([xml-source]
   (xml-source->edn xml-source {}))

  ([xml-source opts]
   (let [additional-args (select-keys opts [:allocator
                                            :coalescing
                                            :namespace-aware
                                            :replacing-entity-references
                                            :supporting-external-entities
                                            :validating
                                            :reporter
                                            :resolver
                                            :support-dtd
                                            :skip-whitespace])
         flattened-args  (flatten (into [] additional-args))
         parsing-args    (cons xml-source flattened-args)
         parsed-xml      (apply xml/parse parsing-args)]
     (xml->edn' parsed-xml opts))))


;; Parsing EDN as XML

(declare edn->xml)


(defn edn-seq->xml
  "Transform an EDN sequence to the pseudo XML expected by `clojure.data.xml`.
   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings"
  {:added "1.0"}
  ([edn]
   (edn-seq->xml edn {}))

  ([edn opts]
   (mapv #(edn->xml % opts) edn)))


(defn edn-map->xml
  "Transform an EDN map to the pseudo XML expected by `clojure.data.xml`.
   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings"
  {:added "1.0"}
  ([edn]
   (edn-map->xml edn {}))

  ([edn {:keys [to-xml-case? from-xml-case? stringify-values?]
         :as   opts}]
   (let [edn-keys                 (keys edn)
         key-set                  (reduce (fn ->keys [acc v] (conj acc (name v))) #{} edn-keys)
         {attrs true tags false}  (group-by #(impl/edn-attrs-tag? (name %) key-set) edn-keys)
         attrs-set                (reduce (fn ->attrs [acc v] (conj acc (impl/attrs-tag->tag (name v)))) #{} attrs)
         tag-generator            (fn tag-generator-fn
                                    [t]
                                    (let [xml-tag     (impl/tagify t to-xml-case?)
                                          xml-content (edn->xml (get edn t) opts)
                                          xml-attrs   (when (contains? attrs-set (name t))
                                                        (->> (get edn (impl/tag->attrs-tag t from-xml-case?))
                                                             (reduce-kv (fn [m k v]
                                                                          (assoc m
                                                                                 (impl/tagify k to-xml-case?)
                                                                                 (impl/stringify v stringify-values?)))
                                                                        {})))]
                                      {:tag     xml-tag
                                       :content xml-content
                                       :attrs   xml-attrs}))]
     (if (= 1 (count tags))
       (tag-generator (first tags))
       (mapv tag-generator tags)))))


(defn edn->xml
  "Transform an EDN data structure to the pseudo XML expected by `clojure.data.xml`.
   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings"
  {:added "1.0"}
  ([edn]
   (edn->xml edn {}))

  ([edn {:keys [stringify-values?] :as opts}]
   (cond
     (impl/string-or-nil? edn)
     [edn]

     (sequential? edn)
     (edn-seq->xml edn opts)

     (and (map? edn)
          (empty? edn))
     {}

     (map? edn)
     (edn-map->xml edn opts)

     (and stringify-values?
          (some? edn))
     (str edn))))


(defn edn->xml-str
  "Transform an EDN data structure into an XML string via `clojure.data.xml`.

   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings

   It also surfaces the original options from `clojure.data.xml/emit-str`
     encoding - The character encoding to use
     doctype - The DOCTYPE declaration to use"
  {:added "1.0"}
  ([edn]
   (edn->xml-str edn {}))

  ([edn {:keys [encoding doctype]
         :as   opts}]
   (let [c-xml (cond
                 (and encoding doctype) (fn emit [edn] (xml/emit-str edn :encoding encoding :doctype doctype))
                 encoding               (fn emit [edn] (xml/emit-str edn :encoding encoding))
                 doctype                (fn emit [edn] (xml/emit-str edn :doctype doctype))
                 :else                  xml/emit-str)]
     (-> edn
         (edn->xml opts)
         c-xml))))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}


(defn edn->xml-stream
  "Transform an EDN data structure into XML and stream is out via `clojure.data.xml`.

   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings

   It also surfaces the original options from `clojure.data.xml/emit`
     encoding - The character encoding to use
     doctype - The DOCTYPE declaration to use"
  {:added "1.0"}
  ([edn java-writer]
   (edn->xml-stream edn java-writer {}))

  ([edn java-writer {:keys [encoding doctype]
                     :as   opts}]
   (let [c-xml (cond
                 (and encoding doctype) (fn emit [edn] (xml/emit edn java-writer :encoding encoding :doctype doctype))
                 encoding               (fn emit [edn] (xml/emit edn java-writer :encoding encoding))
                 doctype                (fn emit [edn] (xml/emit edn java-writer :doctype doctype))
                 :else                  (fn emit [edn] (xml/emit edn java-writer)))]
     (-> edn
         (edn->xml opts)
         c-xml))))
