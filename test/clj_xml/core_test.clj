(ns clj-xml.core-test
  (:require [clj-xml.core :as sut]
            [clojure.test :refer [deftest is testing]]))

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

(deftest xml->edn-test
  (testing "Functional correctness"
    (is (= (sut/xml->edn xml-example) edn-example))
    (is (= (sut/xml->edn xml-example {:preserve-keys? true}) edn-example-original-keys))
    (is (= (sut/xml->edn xml-example {:preserve-attrs? true}) edn-example-with-attrs))
    (is (= (sut/xml->edn xml-example {:preserve-attrs? true :remove-empty-attrs? true}) (update-in edn-example-with-attrs [:test-document :file] dissoc :groups-attrs)))
    (is (= (sut/xml->edn xml-example {:preserve-keys? true :preserve-attrs? true}) edn-example-with-attrs-and-original-keys))
    (is (nil? (sut/xml->edn nil)))
    (is (nil? (sut/xml->edn :edn)))
    (is (= (sut/xml->edn {}) {}))
    (is (= (sut/xml->edn "XML") "XML"))))

(deftest edn->xml-test
  (testing "Functional correctness"
    (is (= xml-example (sut/edn->xml (sut/xml->edn xml-example {:preserve-attrs? true}) {:to-xml-case? true :stringify-values? true})))
    (is (= (sut/edn->xml edn-example-with-attrs-and-original-keys {:to-xml-case? true :from-xml-case? true :stringify-values? true}) xml-example))
    (is (= (sut/edn->xml edn-example-with-attrs {:to-xml-case? true :stringify-values? true}) xml-example))
    (is (= (sut/edn->xml nil) [nil]))
    (is (nil? (sut/edn->xml :edn)))
    (is (= (sut/edn->xml {}) {}))
    (is (= (sut/edn->xml "XML") ["XML"]))
    (is (= (sut/edn->xml 100 {:stringify-values? true}) "100"))
    (is (nil? (sut/edn->xml 100)))))

(def xml-test-string
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\"><HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA><META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD><FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\"><GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT><SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>")

(deftest xml-string-tests
  (testing "functional correctness"
    (is (= xml-test-string (sut/edn->xml-str (sut/xml-str->edn xml-test-string {:preserve-attrs? true :support-dtd false :remove-newlines? true}) {:to-xml-case? true})))))

(deftest insignificant-whitespace-test
  (testing "Corner-cases around embedded insignificant whitespace"
    (let [no-ws (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     "<someTag><foo>wurdz</foo> \n"
                     "</someTag>")
          ws (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  "<someTag> <foo>wurdz</foo> \n"
                  "</someTag>")]
      (is (= {:sometag [{:foo "wurdz"}]} (sut/xml-str->edn no-ws {:skip-whitespace true})))
      (is (= {:sometag [" " {:foo "wurdz"}]} (sut/xml-str->edn ws)))
      (is (= {:sometag [{:foo "wurdz"}]} (sut/xml-str->edn ws {:skip-whitespace true})))
      (is (= {:sometag {:foo "wurdz", :bar "bla"}} (sut/xml-str->edn (str "<sometag><foo>wurdz</foo><bar>bla</bar></sometag>") {:skip-whitespace true}))))))
