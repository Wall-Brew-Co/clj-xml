(ns clj-xml.samples.styles-test
  "Tests clj-xml against the styles data collected by common-beer-data"
   (:require [clj-xml.core :as xml]
             [clojure.java.io :as io]
             [clojure.test :refer :all]
             [matcher-combinators.test]))

  (def parsed-xml
    "Parsed style XML.
       Adapted from: https://github.com/Wall-Brew-Co/common-beer-data/blob/master/exports/xml/styles.xml"
    (-> "styles.xml"
        io/resource
        io/input-stream
        (xml/xml-source->edn {:skip-whitespace true})))

  (deftest styles
    (testing "Style XML may be parsed into a sufficiently useful format"
      (is (match? parsed-xml
                  (-> parsed-xml xml/edn->xml-str xml/xml-str->edn))
          "Parsed data can be round-tripped")
      (is (= 112 (count (:styles parsed-xml))))
      (is (= (count (:styles parsed-xml))
             (->> parsed-xml
                  :styles
                  (map #(:name (:style %)))
                  distinct
                  count)))
      (is (map? parsed-xml))
      (is (coll? (:styles parsed-xml)))
      (is (every? map? (:styles parsed-xml)))
      (is (every? string? (->> parsed-xml
                               :styles
                               (map #(:name (:style %))))))
      (is (= "American Light Lager"
             (-> parsed-xml
                 :styles
                 first
                 :style
                 :name)))
      (is (every? #(contains? (:style %) :carb-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :category) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :carb-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :fg-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-color-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :og-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :fg-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :abv-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-og-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :og-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :name) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-color-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-fg-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-og-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :color-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :type) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :style-letter) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :abv-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :fg-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :category-number) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :carb-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :ibu-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :ingredients) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :examples) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :ibu-range) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :notes) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :og-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :display-fg-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :color-min) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :abv-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :version) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :color-max) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :profile) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :style-guide) (:styles parsed-xml)))
      (is (every? #(contains? (:style %) :ibu-min) (:styles parsed-xml)))))
