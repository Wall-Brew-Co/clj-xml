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
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes"
  ([xml-seq]
   (xml-seq->edn xml-seq {}))

  ([xml-seq opts]
   (let [xml-transformer (fn [x] (xml->edn x opts))]
     (if (and (impl/unique-tags? xml-seq) (> (count xml-seq) 1))
       (reduce into {} (mapv xml-transformer xml-seq))
       (if (or (string? (first xml-seq)) (nil? (first xml-seq)))
         (xml-transformer (first xml-seq))
         (mapv xml-transformer xml-seq))))))

(defn xml-map->edn
  "Transform an XML map as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.
   To change this behavior, an option map be provided with the following keys:
   preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
   preserve-attrs? - to maintain embedded XML attributes"
  ([xml-map]
   (xml-map->edn xml-map {}))

  ([{:keys [tag attrs content]} {:keys [preserve-keys? preserve-attrs? stringify-values?] :as opts}]
   (let [kw-function  (fn [k] (if preserve-keys? k (impl/xml-tag->keyword k)))
         val-function (fn [v] (if stringify-values? (str v) v))
         edn-tag      (kw-function tag)]
     (if (and attrs preserve-attrs?)
       (let [attrs-suffix (if preserve-keys? "_ATTRS" "-attrs")
             attrs-key    (keyword (str (name edn-tag) attrs-suffix))
             attrs-val    (impl/update-vals (impl/update-keys attrs kw-function) val-function)]
         {edn-tag   (xml->edn content opts)
          attrs-key attrs-val})
       {edn-tag (xml->edn content opts)}))))

(defn xml->edn
  "Transform an XML document as formatted by `clojure.xml/parse`, and transform it into normalized EDN.
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.
   To change this behavior, an option map may be provided with the following keys:
   preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
   preserve-attrs? - to maintain embedded XML attributes
   stringify-values? - to coerce non-nil, non-string, non-collection values to strings"
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
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings
   
   
   It also surfaces the original options from `clojure.data.xml/parse-str`
     include-node? - a subset of #{:element :characters :comment} default #{:element :characters}
     location-info - pass false to skip generating location meta data"
  ([xml-str]
   (xml-str->edn xml-str {}))

  ([xml-str {:keys [:include-node? :location-info]
             :as   opts}]
   (let [c-xml (cond
                 (and include-node? location-info) (fn [edn] (xml/parse-str edn :include-node? include-node? :location-info location-info))
                 include-node?                     (fn [edn] (xml/parse-str edn :include-node? include-node?))
                 location-info                     (fn [edn] (xml/parse-str edn :location-info location-info))
                 :else                             (fn [edn] (xml/parse-str edn)))]
     (-> xml-str
         impl/deformat
         c-xml
         (xml->edn opts)))))

(defn xml-source->edn
  "Parse an XML document source with `clojure.xml/parse` and transform it into normalized EDN.
   `xml-source` may be an instance of java.io.InputStream or java.io.Reader
   By default, this also mutates keys from XML_CASE to lisp-case and ignores XML attributes within tags.
   
   To change this behavior, an option map may be provided with the following keys:
     preserve-keys? - to maintain the exact keyword structure provided by `clojure.xml/parse`
     preserve-attrs? - to maintain embedded XML attributes
     stringify-values? - to coerce non-nil, non-string, non-collection values to strings

   It also surfaces the original options from `clojure.data.xml/parse`
     include-node? - a subset of #{:element :characters :comment} default #{:element :characters}
     location-info - pass false to skip generating location meta data"
  ([xml-source]
   (xml-source->edn xml-source {}))

  ([xml-source {:keys [:include-node? :location-info]
                :as   opts}]
   (let [c-xml (cond
                 (and include-node? location-info) (fn [edn] (xml/parse edn :include-node? include-node? :location-info location-info))
                 include-node?                     (fn [edn] (xml/parse edn :include-node? include-node?))
                 location-info                     (fn [edn] (xml/parse edn :location-info location-info))
                 :else                             (fn [edn] (xml/parse edn)))]
     (-> xml-source
         c-xml
         (xml->edn opts)))))

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

  ([edn {:keys [to-xml-case? from-xml-case? stringify-values?] :as opts}]
   (let [edn-keys (keys edn)
         key-set (set (map name edn-keys))
         {attrs true tags false} (group-by #(impl/edn-attrs-tag? (name %) key-set) edn-keys)
         attrs-set (set (map #(impl/attrs-tag->tag (name %)) attrs))
         kw-function (fn [k] (if to-xml-case? (impl/keyword->xml-tag k) k))
         val-function (fn [v] (if stringify-values? (str v) v))
         tag-generator (fn [t]
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
