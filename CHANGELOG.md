## v1.6.1 / 2021 May 06

> This release fixes a typo in the project manifest

## v1.6.0 / 2021 Apr 23

> This release an option to selectively force children node into sequences

* **Add** - add `force-seq-for-paths` option to `xml-str->edn` and `xml-source->edn`

## v1.5.0 / 2021 Apr 19

> This release adds an option to force children node into sequences

* **Add** - add `force-seq?` option to `xml-str->edn` and `xml-source->edn`

## v1.4.0 / 2021 Apr 16

> This release ...

* **Add** - the `skip-whitespace` option to remove whitespace between tags
* **Fix** - parsing of whitespace separator elements as emitted by clojure.data/xml

## v1.3.0 / 2021 Mar 17

> This release adds the `remove-newlines?` option for `xml-str->edn`

## v1.2.0 / 2021 Mar 13

> This release enables XMLInputFactory options for all parsers

## v1.1.0 / 2020 Dec 14

> This release allows consumers to prune empty -attrs maps

* **Add** `remove-empty-attrs?` options

## v1.0.0 / 2020 Dec 12

> This release adds all initial functionality
