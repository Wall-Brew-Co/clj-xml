(ns clj-xml.impl-test
  (:require [clj-xml.impl :as sut]
            [clojure.test :refer :all]))


(deftest tag-keyword-conversion-test
  (testing "Tag formatted and EDN formatted keywords can be transformed"
    (is (= :XML_TAG (sut/keyword->xml-tag :xml-tag)))
    (is (= :edn-keyword (sut/xml-tag->keyword :EDN_KEYWORD)))
    (is (= :XML_TAG (sut/keyword->xml-tag (sut/xml-tag->keyword :XML_TAG))))
    (is (= :edn-keyword (sut/xml-tag->keyword (sut/keyword->xml-tag :edn-keyword))))))


(deftest unique-tags?-test
  (testing "Functional correctness"
    (is (true? (sut/unique-tags? [{:tag "One"} {:tag "Two"} {:tag "Three"}])))
    (is (true? (sut/unique-tags? [])))
    (is (true? (sut/unique-tags? [{:tag "One"}])))
    (is (false? (sut/unique-tags? [{:tag "One"} {:tag "One"}])))))


(deftest attrs-tag->tag-test
  (testing "Functional correctness"
    (is (= "HTML" (sut/attrs-tag->tag "HTML_ATTRS")))
    (is (= "edn" (sut/attrs-tag->tag "edn-attrs")))))


(deftest tag->attrs-tag-test
  (testing "Functional correctness"
    (is (= :HTML-attrs (sut/tag->attrs-tag "HTML" false)))
    (is (= :HTML-attrs (sut/tag->attrs-tag :HTML false)))
    (is (= :HTML_ATTRS (sut/tag->attrs-tag "HTML" true)))
    (is (= :HTML_ATTRS (sut/tag->attrs-tag :HTML true)))
    (is (= :edn-attrs (sut/tag->attrs-tag "edn" false)))))


(deftest update-vals-test
  (testing "Functional correctness"
    (is (= {:a 2 :b 3} (sut/update-vals* {:a 1 :b 2} inc)))
    (is (= {} (sut/update-vals* {} dec)))
    (is (= {:b 3 :c 4} (sut/update-vals* {:b 1 :c 2} + 2)))))


(deftest update-keys-test
  (testing "Functional correctness"
    (is (= {"a" 2 "b" 3} (sut/update-keys* {:a 2 :b 3} name)))
    (is (= {} (sut/update-keys* {} dec)))
    (is (= {":b-key" 3 ":c-key" 4} (sut/update-keys* {:b 3 :c 4} str "-key")))))


(deftest keywordify-test
  (testing "Call through to `xml-tag->keyword` if `preserve-keys?` is false"
    (is (= :xml-tag
           (sut/keywordify :xml-tag false)
           (sut/xml-tag->keyword :xml-tag)))
    (is (= :xml-tag
           (sut/keywordify :XML_TAG false)
           (sut/xml-tag->keyword :XML_TAG))))
  (testing "Returns the original value otherwise"
    (is (= :xml-tag (sut/keywordify :xml-tag true)))
    (is (= :XML_TAG (sut/keywordify :XML_TAG true)))))


(deftest tagify-test
  (testing "Call through to `keyword->xml-tag` if `to-xml-case?` is true"
    (is (= :XML_TAG
           (sut/tagify :xml-tag true)
           (sut/keyword->xml-tag :xml-tag)))
    (is (= :XML_TAG
           (sut/tagify :XML_TAG true)
           (sut/keyword->xml-tag :XML_TAG))))
  (testing "Returns the original value otherwise"
    (is (= :xml-tag
           (sut/tagify :xml-tag false)))
    (is (= :XML_TAG
           (sut/tagify :XML_TAG false)))))


(deftest stringify-test
  (testing "Calls str if `stringify-values?` is true"
    (is (= ":xml"
           (str :xml)
           (sut/stringify :xml true)))
    (is (= "xml"
           (str "xml")
           (sut/stringify "xml" true))))
  (testing "Returns the original value otherwise"
    (is (= :xml
           (sut/stringify :xml false)))
    (is (= "xml"
           (sut/stringify "xml" false)))))


(deftest map*-test
  (is (instance? clojure.lang.LazySeq (sut/map* inc [1 2 4] true)))
  (is (not (instance? clojure.lang.LazySeq (sut/map* inc [1 2 4] false))))
  (is (vector? (sut/map* inc [1 2 4] false))))
