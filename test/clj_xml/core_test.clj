(ns clj-xml.core-test
  (:require [clj-xml.core :as sut]
            [clojure.string :as cs]
            [clojure.test :as t]))


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
       :attrs {}
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


(def edn-example
  {:test-document
   {:head [{:meta-data "Some Fake Data!"}
           {:meta-data "Example Content"}]
    :file {:groups [{:group "test-data-club"}]
           :segments [{:segment "more data"}
                      {:segment "more fake data"}]}}})


(def edn-example-original-keys
  {:TEST_DOCUMENT
   {:HEAD [{:META_DATA "Some Fake Data!"}
           {:META_DATA "Example Content"}]
    :FILE {:GROUPS [{:GROUP "test-data-club"}]
           :SEGMENTS [{:SEGMENT "more data"}
                      {:SEGMENT "more fake data"}]}}})


(def edn-example-original-keys-force-seq
  {:TEST_DOCUMENT
   [{:HEAD [{:META_DATA "Some Fake Data!"}
            {:META_DATA "Example Content"}]}
    {:FILE [{:GROUPS [{:GROUP "test-data-club"}]}
            {:SEGMENTS [{:SEGMENT "more data"}
                        {:SEGMENT "more fake data"}]}]}]})


(def edn-example-with-targeted-coercion
  {:test-document
   [{:head [{:meta-data "Some Fake Data!"}
            {:meta-data "Example Content"}]
     :file {:groups [{:group "test-data-club"}]
            :segments [[{:segment "more data"}]
                       [{:segment "more fake data"}]]}}]})


(def edn-example-with-attrs
  {:test-document
   {:head [{:meta-data "Some Fake Data!" :meta-data-attrs {:type "title"}}
           {:meta-data "Example Content" :meta-data-attrs {:type "tag"}}]
    :file {:groups [{:group "test-data-club"}]
           :groups-attrs {}
           :segments [{:segment "more data" :segment-attrs {:bits "00111010" :number "58"}}
                      {:segment "more fake data" :segment-attrs {:bytes "10100010" :number "-94"}}]}
    :file-attrs {:poster "JANE DOE <j.doe@fake-email.not-real>"
                 :date "2020/04/12"
                 :subject "TEST DATA"}}
   :test-document-attrs {:xmlns "https://www.fake.not/real"}})


(def edn-example-with-attrs-and-original-keys
  {:TEST_DOCUMENT
   {:HEAD [{:META_DATA "Some Fake Data!" :META_DATA_ATTRS {:TYPE "title"}}
           {:META_DATA "Example Content" :META_DATA_ATTRS {:TYPE "tag"}}]
    :FILE {:GROUPS [{:GROUP "test-data-club"}]
           :GROUPS_ATTRS {}
           :SEGMENTS [{:SEGMENT "more data" :SEGMENT_ATTRS {:BITS "00111010" :NUMBER "58"}}
                      {:SEGMENT "more fake data" :SEGMENT_ATTRS {:BYTES "10100010" :NUMBER "-94"}}]}
    :FILE_ATTRS {:POSTER "JANE DOE <j.doe@fake-email.not-real>"
                 :DATE "2020/04/12"
                 :SUBJECT "TEST DATA"}}
   :TEST_DOCUMENT_ATTRS {:XMLNS "https://www.fake.not/real"}})


(t/deftest xml->edn-test
  (t/testing "Functional correctness"
    (t/is (= (sut/xml->edn xml-example) edn-example))
    (t/is (= (sut/xml->edn xml-example {:preserve-keys? true}) edn-example-original-keys))
    (t/is (= (sut/xml->edn xml-example {:preserve-attrs? true}) edn-example-with-attrs))
    (t/is (= (sut/xml->edn xml-example {:preserve-attrs? true :remove-empty-attrs? true}) (update-in edn-example-with-attrs [:test-document :file] dissoc :groups-attrs)))
    (t/is (= (sut/xml->edn xml-example {:preserve-keys? true :preserve-attrs? true}) edn-example-with-attrs-and-original-keys))
    (t/is (= (sut/xml->edn xml-example {:preserve-keys? true :force-seq? true}) edn-example-original-keys-force-seq))
    (t/is (nil? (sut/xml->edn nil)))
    (t/is (nil? (sut/xml->edn :edn)))
    (t/is (= (sut/xml->edn {}) {}))
    (t/is (= (sut/xml->edn "XML") "XML"))))


(t/deftest edn->xml-test
  (t/testing "Functional correctness"
    (t/is (= xml-example (sut/edn->xml (sut/xml->edn xml-example {:preserve-attrs? true}) {:to-xml-case? true :stringify-values? true})))
    (t/is (= (sut/edn->xml edn-example-with-attrs-and-original-keys {:to-xml-case? true :from-xml-case? true :stringify-values? true}) xml-example))
    (t/is (= (sut/edn->xml edn-example-with-attrs {:to-xml-case? true :stringify-values? true}) xml-example))
    (t/is (= (sut/edn->xml nil) [nil]))
    (t/is (nil? (sut/edn->xml :edn)))
    (t/is (= (sut/edn->xml {}) {}))
    (t/is (= (sut/edn->xml "XML") ["XML"]))
    (t/is (= (sut/edn->xml 100 {:stringify-values? true}) "100"))
    (t/is (nil? (sut/edn->xml 100)))))


(def xml-test-string
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\"
   XSI=\"abc\"><HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA> <META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD><FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\"><GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT><SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>")


(t/deftest xml-string-tests
  (t/testing "functional correctness"
    (t/is (= (cs/replace xml-test-string #"\n   " " ") ;; The formatting of xml-test-string spans multiple lines with spaces for alignment, this is stripped internally
             (sut/edn->xml-str (sut/xml-str->edn xml-test-string {:preserve-attrs? true :support-dtd false :remove-newlines? true}) {:to-xml-case? true})))))


(t/deftest insignificant-whitespace-test
  (t/testing "Corner-cases around embedded insignificant whitespace"
    (let [no-ws (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     "<someTag><foo>some text</foo> \n"
                     "</someTag>")
          ws (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  "<someTag> <foo>some text</foo> \n"
                  "</someTag>")]
      (t/is (= {:sometag [{:foo "some text"}]} (sut/xml-str->edn no-ws {:skip-whitespace true})))
      (t/is (= {:sometag [" " {:foo "some text"} " "]} (sut/xml-str->edn ws {:remove-newlines? true})))
      (t/is (= {:sometag [{:foo "some text"}]} (sut/xml-str->edn ws {:skip-whitespace true})))
      (t/is (= {:sometag {:foo "some text", :bar "bla"}} (sut/xml-str->edn (str "<sometag><foo>some text</foo><bar>bla</bar></sometag>") {:skip-whitespace true}))))))


(t/deftest force-xml-seq-at-path-test
  (t/testing "Parsed XML can coerce child nodes to collections"
    (let [nested-data {:a {:b [1 2 3] :c {:d "e"}}}]
      (t/is (= (sut/force-xml-seq-at-path nested-data [:a :b sut/last-child])
               {:a {:b [1 2 [3]] :c {:d "e"}}}))
      (t/is (= (sut/force-xml-seq-at-path nested-data [:a :b sut/first-child])
               {:a {:b [[1] 2 3] :c {:d "e"}}}))
      (t/is (= (sut/force-xml-seq-at-path nested-data [:a :b sut/every-child])
               {:a {:b [[1] [2] [3]] :c {:d "e"}}}))
      (t/is (= (sut/force-xml-seq-at-path nested-data [:a :c :d])
               {:a {:b [1 2 3] :c {:d ["e"]}}}))
      (t/is (= (sut/force-xml-seq-at-path nested-data [:a :c :f])
               {:a {:b [1 2 3] :c {:d "e" :f [nil]}}}))
      (t/is (thrown-with-msg? IllegalArgumentException
                              #"The key :clj-xml.core/first is incompatible with class clojure.lang.PersistentArrayMap"
              (sut/force-xml-seq-at-path nested-data [sut/first-child])))
      (t/is (thrown-with-msg? IllegalArgumentException
                              #"The key :c is incompatible with class clojure.lang.PersistentVector"
              (sut/force-xml-seq-at-path nested-data [:a :b :c]))))))


(t/deftest force-xml-seq-at-paths-test
  (t/testing "Parsed XML can coerce child nodes to collections"
    (let [nested-data {:a {:b [1 2 3] :c {:d "e"}}}]
      (t/is (= (sut/force-xml-seq-at-paths nested-data [[:a :b sut/first-child] [:a :b sut/last-child] [:a]])
               {:a [{:b [[1] 2 [3]] :c {:d "e"}}]}))
      (t/is (= (sut/force-xml-seq-at-paths nested-data [[:a] [:a sut/first-child :b]])
               {:a [{:b [[1 2 3]] :c {:d "e"}}]})))))


(t/deftest xml-sequence-coercion-test
  (t/testing "parsed XML can be coerced"
    (t/is (= (sut/xml->edn' xml-example {:force-seq-for-paths [[:test-document :file :segments sut/every-child]
                                                               [:test-document]]})
             edn-example-with-targeted-coercion))))
