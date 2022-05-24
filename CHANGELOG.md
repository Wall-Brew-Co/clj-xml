## v1.7.0 / 2022 May 17

> This release updates to use Clojure 1.11.1 and the new `update-vals` and `update-keys` functions.

* **Add** - VS Code/Calva files to gitignore
* **Update** - Update Clojure to 1.11.1. Use new functions in place of existing.

## v1.6.2 / 2021 May 27

> This release fixes an issue parsing hand-formatted XML files that split attributes with newline characters

* **Fix** ^^^

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
