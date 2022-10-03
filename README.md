# clj-xml

[![Clojars Project](https://img.shields.io/clojars/v/com.wallbrew/clj-xml.svg)](https://clojars.org/com.wallbrew/clj-xml)
[![cljdoc badge](https://cljdoc.org/badge/com.wallbrew/clj-xml)](https://cljdoc.org/d/com.wallbrew/clj-xml/CURRENT)
![GitHub Runner](https://github.com/Wall-Brew-Co/clj-xml/workflows/Clojure%20CI/badge.svg)
[![GitHub](https://img.shields.io/github/license/Wall-Brew-Co/clj-xml)](https://github.com/Wall-Brew-Co/clj-xml/blob/master/LICENSE)
[![Twitter Follow](https://img.shields.io/twitter/follow/WallBrew?style=social)](https://twitter.com/WallBrew)

A clojure library designed to make conversions between EDN and XML a little easier.

This repository follows the guidelines and standards of the [Wall Brew Open Source Policy.](https://github.com/Wall-Brew-Co/open-source "Our open source guidelines")

## Installation

A deployed copy of the most recent version of [clj-xml can be found on clojars.](https://clojars.org/com.wallbrew/clj-xml)
To use it, add the following as a dependency in your project.clj file:

[![Clojars Project](https://clojars.org/com.wallbrew/clj-xml/latest-version.svg)](https://clojars.org/com.wallbrew/clj-xml)

The next time you build your application, [Leiningen](https://leiningen.org/) or [deps.edn](https://clojure.org/guides/deps_and_cli) should pull it automatically.
Alternatively, you may clone or fork the repository to work with it directly.

## Public Functions

### XML Parsing

This library is built on top of `clojure.data.xml` and extracts the element structure they've implemented.
Based on where your XML is and how it's stored, you'll want to use one of the three following functions to extract it.

* When you have already parsed an XML document into `clojure.data.xml` elements: `xml->edn`
* When you have a string containing an XML document: `xml-str->edn`
* When you have a `java.io.InputStream` or `java.io.Reader` (generally used for HTTP/File System): `xml-source->edn`

Each of these functions accepts an option map as an optional second argument, supporting the following keys:

| Option                 | Default Value |Description                                                                                                                          |
|------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `:preserve-keys?`      | `false`       |Maintain the exact keyword structure provided by `clojure.xml/parse`                                                                 |
| `:preserve-attrs?`     | `false`       |Maintain embedded XML attributes                                                                                                     |
| `:remove-empty-attrs?` | `false`       |Remove any empty attribute maps                                                                                                      |
| `:stringify-values?`   | `false`       |Coerce non-nil, non-string, non-collection values to strings                                                                         |
| `:remove-newlines?`    | `false`       |Remove any newline characters in `xml-str`. Only applicable for `xml-str->edn`                                                       |
| `:force-seq?`          | `false`       |Coerce all child XML nodes into an array of maps.                                                                                    |
| `:force-seq-for-paths` | `[]`          |A sequence of key-path sequences that will be selectively coerced into sequences. Read more about [Key Pathing](#key-pathing) below. |

`xml-str->edn` and `xml-source->edn` also support the parsing options from `clojure.data.xml` and Java's `XMLInputFactory` class.
[Additional documentation](http://docs.oracle.com/javase/6/docs/api/javax/xml/stream/XMLInputFactory.html) from Oracle is available.
This library does not override the default behavior of `XMLInputFactory`.

| Option                          | Default Value               | Description                                                                             |
|---------------------------------|-----------------------------|-----------------------------------------------------------------------------------------|
| `:include-node?`                | `#{:element :characters}`   | A subset of #{:element :characters :comment}, representing the XML nodes to keep        |
| `:location-info`                | `true`                      | Wether or not location metadata should be generated and attached to results             |
| `:allocator`                    | Object created Just-In-Time | An instance of an XMLInputFactory/ALLOCATOR to allocate events                          |
| `:coalescing`                   | `true`                      | Convert CDATA nodes to text, and append any adjacent text nodes                         |
| `:namespace-aware`              | `false`                     | Wether or not XML 1.0 namespacing support is enabled                                    |
| `:replacing-entity-references`  | `false`                     | Wether or not internal entity text should be replaced with its XML entity form          |
| `:supporting-external-entities` | `false`                     | Wether or not externally hosted entities will be resolved at parsing time and evaluated |
| `:validating`                   | `false`                     | Wether or not attached DTDs will be resolved and used to validate the XML document      |
| `:reporter`                     | Object created Just-In-Time | An instance of a XMLInputFactory/REPORTER to use in place of default                    |
| `:resolver`                     | Object created Just-In-Time | An instance of a XMLInputFactory/RESOLVER to use in place of defaults                   |
| `:support-dtd`                  | `false`                     | Wether or not attached DTDs will be read by the parser                                  |
| `:skip-whitespace`              | `false`                     | Wether or not any whitespace only elements will be preserved as nodes                   |

Let's see how it works:

```clojure
(require [clj-xml.core :as xml])

(def xml-example
  {:tag :TEST_DOCUMENT
   :attrs {:XMLNS "https://www.fake.not/real"}
   :content
   [{:tag :HEAD
     :attrs nil
     :content
     [{:tag :META_DATA :attrs {:TYPE "title"} :content ["Some Fake Data!"]}
      {:tag :META_DATA :attrs {:TYPE "tag"} :content ["Example Content"]}]}
    {:tag :FILE
     :attrs
     {:POSTER "JANE DOE <j.doe@fake-email.not-real>"
      :DATE "2020/04/12"
      :SUBJECT "TEST DATA"}
     :content
     [{:tag :GROUPS
       :attrs nil
       :content
       [{:tag :GROUP :attrs nil :content ["test-data-club"]}]}
      {:tag :SEGMENTS
       :attrs nil
       :content
       [{:tag :SEGMENT
         :attrs {:BITS "00111010" :NUMBER "58"}
         :content ["more data"]}
        {:tag :SEGMENT
         :attrs {:BYTES "10100010" :NUMBER "-94"}
         :content ["more fake data"]}]}]}]})

(xml/xml->edn xml-example)
;; => {:test-document
;;     {:head [{:meta-data "Some Fake Data!"}
;;             {:meta-data "Example Content"}]
;;      :file {:groups [{:group "test-data-club"}]
;;             :segments [{:segment "more data"}
;;                        {:segment "more fake data"}]}}}

;; Parse a string instead
(def xml-test-string
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\"><HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA><META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD><FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\"><GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT><SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>")

(xml/xml-str->edn xml-test-string)
;; => {:test-document
;;     {:head [{:meta-data "Some Fake Data!"}
;;             {:meta-data "Example Content"}]
;;      :file {:groups [{:group "test-data-club"}]
;;             :segments [{:segment "more data"}
;;                        {:segment "more fake data"}]}}}

;; Preserve the XML_CASE
(xml/xml->edn xml-example {:preserve-keys? true})
;; => {:TEST_DOCUMENT
;;     {:HEAD [{:META-DATA "Some Fake Data!"}
;;             {:META-DATA "Example Content"}]
;;      :FILE {:GROUPS [{:GROUP "test-data-club"}]
;;             :SEGMENTS [{:SEGMENT "more data"}
;;                        {:SEGMENT "more fake data"}]}}}

;; Preserve the XML attributes
(xml/xml->edn xml-example {:preserve-attrs? true})
;; =>   {:test-document
;;      {:head [{:meta-data "Some Fake Data!" :meta-data-attrs {:type "title"}}
;;              {:meta-data "Example Content" :meta-data-attrs {:type "tag"}}]
;;       :file {:groups [{:group "test-data-club"}]
;;              :segments [{:segment "more data" :segment-attrs {:bits "00111010" :number "58"}}
;;                         {:segment "more fake data" :segment-attrs {:bytes "10100010" :number "-94"}}]}
;;       :file-attrs {:poster "JANE DOE <j.doe@fake-email.not-real>"
;;                    :date "2020/04/12"
;;                    :subject "TEST DATA"}}
;;      :test-document-attrs {:xmlns "https://www.fake.not/real"}}
```

#### Key Pathing

When you want to ensure selective paths in the returned XML are coerced to sequences, you may pick one of two options:

* `force-seq?`          - to coerce all child XML nodes into a collection of nodes.
* `force-seq-for-paths` - to coerce selective child XML nodes into collections of nodes.

In the case of `force-seq-for-paths`, you will supply a sequence of key paths, each of which direct to children a la `assoc-in`.
This key path may contain three types of paths, which may be used together.

* The namespace qualified keywords: `:clj-xml.core/first`, `:clj-xml.core/last`, and `:clj-xml.core/every`
  * These will modify a sequence's first, last, or every child, respectively
* The provided alias symbols for the above keywords: `first-child`, `last-child`, and `every-child`
  * These will modify a sequence's first, last, or every child, respectively
* Bare keywords
  * These will modify the matching given keyword in a map a la `update`

If the key path is incongruent with the current data structure, (e.g. `every-child` and a hash-map), an exception will be thrown.

```clojure
(require [clj-xml.core :as xml])

(xml/xml->edn' xml-example {:force-seq-for-paths [[:test-document :file :segments xml/every-child]
                                                  [:test-document]]})
;; => {:test-document
;;     [{:head [{:meta-data "Some Fake Data!"}
;;              {:meta-data "Example Content"}]
;;      :file {:groups [{:group "test-data-club"}]
;;             :segments [[{:segment "more data"}]
;;                        [{:segment "more fake data"}

(xml/xml->edn' xml-example {:force-seq-for-paths [[xml/every-child]]})
;; => java.lang.IllegalArgumentException: The key clj-xml.core/every is incompatible with class clojure.lang.PersistentArrayMap
```

### XML Emission

To convert EDN into XML, you'll want to use one of the following functions based on the target location.

* When you want `clojure.data.xml` elements: `edn->xml`
* When you want an unindented string containing an XML document: `edn->xml-str`
* When you have a `java.io.OutputputStream` or `java.io.Writer` (generally used for HTTP/File System): `edn->xml-stream`

Each of these functions accepts an option map as an optional final argument, supporting the following keys:

| Option               | Default Value | Description                                                                           |
|----------------------|---------------|---------------------------------------------------------------------------------------|
| `:to-xml-case?`      | `false`       | Wether or not the keys representing XML tags will be coerced to XML_CASE              |
| `:from-xml-case?`    | `false`       | Wether or not the source EDN is in XML_CASE                                           |
| `:stringify-values?` | `false`       | Wether or not non-nil, non-string, non-collection values should be coerced to strings |

`edn->xml-str` and `edn->xml-stream` also support the parsing options from `clojure.data.xml`:

| Option      | Default Value | Description                                                          |
|-------------|---------------|----------------------------------------------------------------------|
| `:encoding` | `UTF-8`       | A string representing the character encoding the encoder should emit |
| `:doctype`  | `nil`         | An XML Doctype that should be written as the emitted document's DTD  |

Let's see how it works:

```clojure
(require [clj-xml.core :as xml])

(def edn-example-with-attrs-and-original-keys
  {:TEST_DOCUMENT
   {:HEAD [{:META_DATA "Some Fake Data!" :META_DATA_ATTRS {:TYPE "title"}}
           {:META_DATA "Example Content" :META_DATA_ATTRS {:TYPE "tag"}}]
    :FILE {:GROUPS [{:GROUP "test-data-club"}]
           :SEGMENTS [{:SEGMENT "more data" :SEGMENT_ATTRS {:BITS "00111010" :NUMBER "58"}}
                      {:SEGMENT "more fake data" :SEGMENT_ATTRS {:BYTES "10100010" :NUMBER "-94"}}]}
    :FILE_ATTRS {:POSTER "JANE DOE <j.doe@fake-email.not-real>"
                 :DATE "2020/04/12"
                 :SUBJECT "TEST DATA"}}
   :TEST_DOCUMENT_ATTRS {:XMLNS "https://www.fake.not/real"}})

(xml/edn->xml-str edn-example-with-attrs-and-original-keys {:to-xml-case? true :from-xml-case? true :stringify-values? true})
;; => "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\"><HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA><META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD><FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\"><GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT><SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>"

```

## Contributors

<a href="https://github.com/Wall-Brew-Co/clj-xml/graphs/contributors"><img src="https://raw.githubusercontent.com/Wall-Brew-Co/clj-xml/master/CONTRIBUTORS.svg" alt="The GitHub profile pictures of all current contributors. Clicking this image will lead you to the GitHub contribution graph." /></a>

## License

Copyright © 2020-2022 - [Wall Brew Co](https://wallbrew.com/)

This software is provided for free, public use as outlined in the [MIT License](https://github.com/Wall-Brew-Co/clj-xml/blob/master/LICENSE)
