(ns clj-xml.core
  "A bunch of utility functions for xml documents"
  (:require [clj-xml.impl :as impl]
            [clojure.data.xml :as xml]))

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
  ([xml-seq]
   (xml-seq->edn xml-seq {}))

  ([xml-seq {:keys [stringify-values? 
                    force-seq?]
             :as   opts}]
   (let [xml-transformer (fn [x] (xml->edn x opts))]
     (cond
       (or (nil? xml-seq)
           (string? xml-seq))               xml-seq
       (and (= 1 (count xml-seq))
            (or (nil? (first xml-seq))
                (string? (first xml-seq)))) (first xml-seq)
       (and (impl/unique-tags? xml-seq)
            (> (count xml-seq) 1)
            (not force-seq?))               (reduce into {} (mapv xml-transformer xml-seq))
       (and (map? xml-seq)
            (empty? xml-seq))               {}
       (map? xml-seq)                       (xml-transformer xml-seq)
       (sequential? xml-seq)                (mapv xml-transformer xml-seq)
       (and stringify-values?
            (some? xml-seq))                (str xml-seq)))))

(defn xml-map->edn
  "Transform an XML map as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map be provided with the following keys:
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps"
  ([xml-map]
   (xml-map->edn xml-map {}))

  ([{:keys [tag attrs content]} {:keys [preserve-keys? preserve-attrs? stringify-values? remove-empty-attrs?]
                                 :as   opts}]
   (let [kw-function  (fn [k] (if preserve-keys? k (impl/xml-tag->keyword k)))
         val-function (fn [v] (if stringify-values? (str v) v))
         edn-tag      (kw-function tag)
         edn-value    (xml->edn content opts)]
     (if (and attrs preserve-attrs?)
       (let [attrs-suffix (if preserve-keys? "_ATTRS" "-attrs")
             attrs-key    (keyword (str (name edn-tag) attrs-suffix))
             attrs-val    (impl/update-vals (impl/update-keys attrs kw-function) val-function)
             add-attrs?   (or (not remove-empty-attrs?)
                              (and remove-empty-attrs? (seq attrs-val)))]
         (merge {edn-tag edn-value}
                (when add-attrs? {attrs-key attrs-val})))
       {edn-tag edn-value}))))

(defn xml->edn
  "Transform an XML document as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.
   To change this behavior, an option map may be provided with the following keys:
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps"
  ([xml-doc]
   (xml->edn xml-doc {}))

  ([xml-doc {:keys [stringify-values?] :as opts}]
   (cond
     (or (nil? xml-doc)
         (string? xml-doc)) xml-doc
     (sequential? xml-doc)  (xml-seq->edn xml-doc opts)
     (and (map? xml-doc)
          (empty? xml-doc)) {}
     (map? xml-doc)         (xml-map->edn xml-doc opts)
     (and stringify-values?
          (some? xml-doc))  (str xml-doc))))

(defn xml-str->edn
  "Parse an XML document with `clojure.xml/parse-str` and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map may be provided with the following keys:
     preserve-keys?      - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - to maintain embedded XML attributes
     stringify-values?   - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps
     remove-newlines?    - to remove any newline characters in `xml-str`
     force-seq?          - to coerce child XML nodes into a sequence of maps

   It also surfaces the original options from `clojure.data.xml/parse-str`
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
     (xml->edn parsed-xml opts))))

(defn xml-source->edn
  "Parse an XML document source with `clojure.xml/parse` and transform it into normalized EDN.
   `xml-source` may be an instance of java.io.InputStream or java.io.Reader
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.

   To change this behavior, an option map may be provided with the following keys:
     preserve-keys?      - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs?     - to maintain embedded XML attributes
     stringify-values?   - to coerce non-nil, non-string, non-collection values to strings
     remove-empty-attrs? - to remove any empty attribute maps

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
     (xml->edn parsed-xml opts))))

;; Parsing EDN as XML

(declare edn->xml)

(defn edn-seq->xml
  "Transform an EDN sequence to the pseudo XML expected by `clojure.data.xml`.
   To change the default behavior, an option map may be provided with the following keys:
   to-xml-case? - To modify the keys representing XML tags to XML_CASE
   from-xml-case? - If the source EDN has XML_CASE keys
   stringify-values? - to coerce non-nil, non-string, non-collection values to strings"
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
  ([edn]
   (edn-map->xml edn {}))

  ([edn {:keys [to-xml-case? from-xml-case? stringify-values?]
         :as   opts}]
   (let [edn-keys                 (keys edn)
         key-set                  (set (map name edn-keys))
         {attrs true tags false}  (group-by #(impl/edn-attrs-tag? (name %) key-set) edn-keys)
         attrs-set                (set (map #(impl/attrs-tag->tag (name %)) attrs))
         kw-function              (fn [k] (if to-xml-case? (impl/keyword->xml-tag k) k))
         val-function             (fn [v] (if stringify-values? (str v) v))
         tag-generator            (fn [t]
                                    (let [xml-tag     (kw-function t)
                                          xml-content (edn->xml (get edn t) opts)
                                          xml-attrs   (when (contains? attrs-set (name t))
                                                        (-> (get edn (impl/tag->attrs-tag t from-xml-case?))
                                                            (impl/update-keys kw-function)
                                                            (impl/update-vals val-function)))]
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
  ([edn]
   (edn->xml edn {}))

  ([edn {:keys [stringify-values?] :as opts}]
   (cond
     (or (nil? edn)
         (string? edn))     [edn]
     (sequential? edn)      (edn-seq->xml edn opts)
     (and (map? edn)
          (empty? edn))     {}
     (map? edn)             (edn-map->xml edn opts)
     (and stringify-values?
          (some? edn))      (str edn))))

(defn edn->xml-str
  "Transform an EDN data structure into an XML string via `clojure.data.xml`.

   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings

   It also surfaces the original options from `clojure.data.xml/emit-str`
     encoding - The character encoding to use
     doctype - The DOCTYPE declaration to use"
  ([edn]
   (edn->xml-str edn {}))

  ([edn {:keys [:encoding :doctype]
         :as   opts}]
   (let [c-xml (cond
                 (and encoding doctype) (fn [edn] (xml/emit-str edn :encoding encoding :doctype doctype))
                 encoding               (fn [edn] (xml/emit-str edn :encoding encoding))
                 doctype                (fn [edn] (xml/emit-str edn :doctype doctype))
                 :else                  (fn [edn] (xml/emit-str edn)))]
     (-> edn
         (edn->xml opts)
         c-xml))))

(defn edn->xml-stream
  "Transform an EDN data structure into XML and stream is out via `clojure.data.xml`.

   To change the default behavior, an option map may be provided with the following keys:
     to-xml-case? - To modify the keys representing XML tags to XML_CASE
     from-xml-case? - If the source EDN has XML_CASE keys
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings

   It also surfaces the original options from `clojure.data.xml/emit`
     encoding - The character encoding to use
     doctype - The DOCTYPE declaration to use"
  ([edn java-writer]
   (edn->xml-stream edn java-writer {}))

  ([edn java-writer {:keys [:encoding :doctype]
                     :as   opts}]
   (let [c-xml (cond
                 (and encoding doctype) (fn [edn] (xml/emit edn java-writer :encoding encoding :doctype doctype))
                 encoding               (fn [edn] (xml/emit edn java-writer :encoding encoding))
                 doctype                (fn [edn] (xml/emit edn java-writer :doctype doctype))
                 :else                  (fn [edn] (xml/emit edn java-writer)))]
     (-> edn
         (edn->xml opts)
         c-xml))))
