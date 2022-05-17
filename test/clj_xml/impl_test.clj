(ns clj-xml.impl-test
  (:require [clj-xml.impl :as sut]
            [clojure.test :as t]))


(t/deftest tag-keyword-conversion-test
  (t/testing "Tag formatted and EDN formatted keywords can be transformed"
    (t/is (= :XML_TAG (sut/keyword->xml-tag :xml-tag)))
    (t/is (= :edn-keyword (sut/xml-tag->keyword :EDN_KEYWORD)))
    (t/is (= :XML_TAG (sut/keyword->xml-tag (sut/xml-tag->keyword :XML_TAG))))
    (t/is (= :edn-keyword (sut/xml-tag->keyword (sut/keyword->xml-tag :edn-keyword))))))


(t/deftest unique-tags?-test
  (t/testing "Functional correctness"
    (t/is (true? (sut/unique-tags? [{:tag "One"} {:tag "Two"} {:tag "Three"}])))
    (t/is (true? (sut/unique-tags? [])))
    (t/is (true? (sut/unique-tags? [{:tag "One"}])))
    (t/is (false? (sut/unique-tags? [{:tag "One"} {:tag "One"}])))))


(t/deftest attrs-tag->tag-test
  (t/testing "Functional correctness"
    (t/is (= "HTML" (sut/attrs-tag->tag "HTML_ATTRS")))
    (t/is (= "edn" (sut/attrs-tag->tag "edn-attrs")))))


(t/deftest tag->attrs-tag-test
  (t/testing "Functional correctness"
    (t/is (= :HTML-attrs (sut/tag->attrs-tag "HTML" false)))
    (t/is (= :HTML-attrs (sut/tag->attrs-tag :HTML false)))
    (t/is (= :HTML_ATTRS (sut/tag->attrs-tag "HTML" true)))
    (t/is (= :HTML_ATTRS (sut/tag->attrs-tag :HTML true)))
    (t/is (= :edn-attrs (sut/tag->attrs-tag "edn" false)))))
