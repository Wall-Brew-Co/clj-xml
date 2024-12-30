(ns clj-xml.samples.fermentables-test
  "Tests clj-xml against the fermentables data collected by common-beer-data"
  (:require [clj-xml.core :as xml]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [matcher-combinators.test]))

(def parsed-xml
  "Parsed fermentable XML.
     Adapted from: https://github.com/Wall-Brew-Co/common-beer-data/blob/master/exports/xml/fermentables.xml"
  (-> "fermentables.xml"
      io/resource
      io/input-stream
      (xml/xml-source->edn {:skip-whitespace true})))

(deftest fermentables
  (testing "Fermentable XML may be parsed into a sufficiently useful format"
    (is (match? parsed-xml
                (-> parsed-xml xml/edn->xml-str xml/xml-str->edn))
        "Parsed data can be round-tripped")
    (is (= 101 (count (:fermentables parsed-xml))))
    (is (= (count (:fermentables parsed-xml))
           (->> parsed-xml
                :fermentables
                (map #(:name (:fermentable %)))
                distinct
                count)))
    (is (map? parsed-xml))
    (is (coll? (:fermentables parsed-xml)))
    (is (every? map? (:fermentables parsed-xml)))
    (is (every? string? (->> parsed-xml
                             :fermentables
                             (map #(:name (:fermentable %))))))
    (is (= "Clear Candi Sugar"
           (-> parsed-xml
               :fermentables
               first
               :fermentable
               :name)))
    (is (every? #(contains? (:fermentable %) :amount) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :yield) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :color) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :potential) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :name) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :type) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :add-after-boil) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :notes) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :version) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :max-in-batch) (:fermentables parsed-xml)))
    (is (every? #(contains? (:fermentable %) :recommend-mash) (:fermentables parsed-xml)))))
