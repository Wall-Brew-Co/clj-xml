(ns clj-xml.impl-test
  (:require [clj-xml.impl :as sut]
            [clojure.test :refer [deftest is testing]]))

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
    (is (= {:a 2 :b 3} (sut/update-vals {:a 1 :b 2} inc)))
    (is (= {} (sut/update-vals {} dec)))
    (is (= {:b 3 :c 4} (sut/update-vals {:b 1 :c 2} + 2)))))

(deftest update-keys-test
  (testing "Functional correctness"
    (is (= {"a" 2 "b" 3} (sut/update-keys {:a 2 :b 3} name)))
    (is (= {} (sut/update-keys {} dec)))
    (is (= {":b-key" 3 ":c-key" 4} (sut/update-keys {:b 3 :c 4} str "-key")))))
