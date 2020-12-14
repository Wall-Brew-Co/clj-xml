# clj-xml

[![Clojars Project](https://img.shields.io/clojars/v/com.wallbrew/clj-xml.svg)](https://clojars.org/com.wallbrew/clj-xml)
[![cljdoc badge](https://cljdoc.org/badge/com.wallbrew/clj-xml)](https://cljdoc.org/d/com.wallbrew/clj-xml/CURRENT)
![GitHub Runner](https://github.com/Wall-Brew-Co/clj-xml/workflows/Clojure%20CI/badge.svg)

A clojure library designed to make conversions between EDN and XML a little easier.

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

* `preserve-keys?` - to maintain the exact keyword structure provided by `clojure.xml/parse`
* `preserve-attrs?` - to maintain embedded XML attributes
* `remove-empty-attrs?` - to remove any empty attribute maps
* `stringify-values?` - to coerce non-nil, non-string, non-collection values to strings

`xml-str->edn` and `xml-source->edn` also support the parsing options from `clojure.data.xml`:

* `include-node?` - a subset of #{:element :characters :comment} default #{:element :characters}
* `location-info` - pass false to skip generating location metadata

Lets see how it works:

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

(xml->edn xml-example)
;; => {:test-document
;;     {:head [{:meta-data "Some Fake Data!"}
;;             {:meta-data "Example Content"}]
;;      :file {:groups [{:group "test-data-club"}]
;;             :segments [{:segment "more data"}
;;                        {:segment "more fake data"}]}}}

;; Parse a string instead
(def xml-test-string
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\"><HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA><META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD><FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\"><GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT><SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>")

(xml-str->edn xml-example)
;; => {:test-document
;;     {:head [{:meta-data "Some Fake Data!"}
;;             {:meta-data "Example Content"}]
;;      :file {:groups [{:group "test-data-club"}]
;;             :segments [{:segment "more data"}
;;                        {:segment "more fake data"}]}}}

;; Preserve the XML_CASE
(xml->edn xml-example {:preserve-keys? true})
;; => {:TEST_DOCUMENT
;;     {:HEAD [{:META-DATA "Some Fake Data!"}
;;             {:META-DATA "Example Content"}]
;;      :FILE {:GROUPS [{:GROUP "test-data-club"}]
;;             :SEGMENTS [{:SEGMENT "more data"}
;;                        {:SEGMENT "more fake data"}]}}}

;; Preserve the XML attributes
(xml->edn xml-example {:preserve-attrs? true})
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

### XML Emission

To convert EDN into XML, you'll want to use one of the following functions based on the target location.

* When you want `clojure.data.xml` elements: `edn->xml`
* When you want an unindented string containing an XML document: `edn->xml-str`
* When you have a `java.io.OutputputStream` or `java.io.Writer` (generally used for HTTP/File System): `edn->xml-stream`

Each of these functions accepts an option map as an optional final argument, supporting the following keys:

* `to-xml-case?` - To modify the keys representing XML tags to XML_CASE
* `from-xml-case?` - If the source EDN has XML_CASE keys
* `stringify-values?` - to coerce non-nil, non-string, non-collection values to strings

`edn->xml-str` and `edn->xml-stream` also support the parsing options from `clojure.data.xml`:

* `encoding` - The character encoding to use
* `doctype` - The DOCTYPE declaration to use

Lets see how it works:

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

## License

Copyright © 2020 - [Wall Brew Co](https://wallbrew.com/)

This software is provided for free, public use as outlined in the [MIT License](https://github.com/Wall-Brew-Co/clj-xml/blob/master/LICENSE)
