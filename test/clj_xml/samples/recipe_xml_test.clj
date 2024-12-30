(ns clj-xml.samples.recipe-xml-test
  "Tests clj-xml against recipe data that we'd encounter in brew-bot"
  (:require [clj-xml.core :as xml]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [matcher-combinators.test]))


(def parsed-xml
  "Parsed recipe XML.
   Adapted from: https://github.com/Wall-Brew-Co/common-beer-format/blob/8894ba3f21b27d441af2e732dae85572ecc0b908/resources/xml/recipes.xml"
  (-> "recipe.xml"
      io/resource
      io/input-stream
      (xml/xml-source->edn {:skip-whitespace  true
                            :limit-eagerness? true})))


(deftest recipe-test
  (testing "Recipe XML may be parsed into a sufficiently useful format"
    (is (match? parsed-xml
                (-> parsed-xml xml/edn->xml-str xml/xml-str->edn))
        "Parsed data can be round-tripped")
    (is (match? parsed-xml
                {:recipes [{:recipe {:age                 "24.0"
                                     :age-temp            "17.0"
                                     :batch-size          "18.93"
                                     :boil-size           "20.82"
                                     :boil-time           "60.0"
                                     :brewer              "Brad Smith"
                                     :carbonation         "2.1"
                                     :carbonation-used    "Kegged"
                                     :date                "3 Jan 04"
                                     :efficiency          "72.0"
                                     :fermentables        [{:fermentable {:amount           "2.27"
                                                                          :coarse-fine-diff "1.5"
                                                                          :color            "3.0"
                                                                          :diastatic-power  "45.0"
                                                                          :max-in-batch     "100.0"
                                                                          :moisture         "4.0"
                                                                          :name             "Pale Malt (2 row) UK"
                                                                          :notes            "All purpose base malt for English styles"
                                                                          :origin           "United Kingdom"
                                                                          :protein          "10.2"
                                                                          :supplier         "Fussybrewer Malting"
                                                                          :type             "Grain"
                                                                          :version          "1"
                                                                          :yield            "78.0"}}
                                                           {:fermentable {:amount           "0.91"
                                                                          :coarse-fine-diff "1.5"
                                                                          :color            "2.0"
                                                                          :diastatic-power  "0.0"
                                                                          :max-in-batch     "20.0"
                                                                          :moisture         "9.0"
                                                                          :name             "Barley, Flaked"
                                                                          :notes            "Adds body to porters and stouts, must be mashed"
                                                                          :origin           "United Kingdom"
                                                                          :protein          "13.2"
                                                                          :recommend-mash   "TRUE"
                                                                          :supplier         "Fussybrewer Malting"
                                                                          :type             "Grain"
                                                                          :version          "1"
                                                                          :yield            "70.0"}}
                                                           {:fermentable {:amount           "0.45"
                                                                          :coarse-fine-diff "1.5"
                                                                          :color            "500.0"
                                                                          :diastatic-power  "0.0"
                                                                          :max-in-batch     "10.0"
                                                                          :moisture         "5.0"
                                                                          :name             "Black Barley"
                                                                          :notes            "Unmalted roasted barley for stouts, porters"
                                                                          :origin           "United Kingdom"
                                                                          :protein          "13.2"
                                                                          :supplier         "Fussybrewer Malting"
                                                                          :type             "Grain"
                                                                          :version          "1"
                                                                          :yield            "78.0"}}],
                                     :fermentation-stages "2"
                                     :fg                  "1.012"
                                     :hops                [{:hop {:alpha   "5.0"
                                                                  :amount  "0.0638"
                                                                  :name    "Goldings, East Kent"
                                                                  :notes   "Great all purpose UK hop for ales, stouts, porters"
                                                                  :time    "60.0"
                                                                  :use     "Boil"
                                                                  :version "1"}}],
                                     :mash                {:grain-temp "22.0"
                                                           :mash-steps [{:mash-step {:infuse-amount "10.0"
                                                                                     :name          "Conversion Step, 68C"
                                                                                     :step-temp     "68.0"
                                                                                     :step-time     "60.0"
                                                                                     :type          "Infusion"
                                                                                     :version       "1"}}],
                                                           :name       "Single Step Infusion, 68 C"
                                                           :version    "1"},
                                     :miscs               [{:misc {:amount  "0.010"
                                                                   :name    "Irish Moss"
                                                                   :notes   "Used as a clarifying agent during the last few minutes of the boil"
                                                                   :time    "15.0"
                                                                   :type    "Fining"
                                                                   :use     "Boil"
                                                                   :version "1"}}],
                                     :name                "Dry Stout"
                                     :og                  "1.036"
                                     :rating              "41"
                                     :style               {:abv-max         "5.5"
                                                           :abv-min         "3.2"
                                                           :carb-max        "2.1"
                                                           :carb-min        "1.6"
                                                           :category        "Stout"
                                                           :category-number "16"
                                                           :color-max       "200.0"
                                                           :color-min       "35.0"
                                                           :fg-max          "1.011"
                                                           :fg-min          "1.007"
                                                           :ibu-max         "50.0"
                                                           :ibu-min         "30.0"
                                                           :name            "Dry Stout"
                                                           :notes           "Famous Irish Stout. Dry, roasted, almost coffee like flavor. Often soured with pasteurized sour beer. Full body perception due to flaked barley, though starting gravity may be low. Dry roasted flavor."
                                                           :og-max          "1.050"
                                                           :og-min          "1.035"
                                                           :style-guide     "BJCP"
                                                           :style-letter    "A"
                                                           :type            "Ale"
                                                           :version         "1"},
                                     :taste-notes         "Nice dry Irish stout with a warm body but low starting gravity much like the famous drafts."
                                     :type                "All Grain"
                                     :version             "1"
                                     :waters              [{:water {:amount      "20.0"
                                                                    :bicarbonate "300.0"
                                                                    :calcium     "295.0"
                                                                    :chloride    "25.0"
                                                                    :magnesium   "45.0"
                                                                    :name        "Burton on Trent, UK"
                                                                    :notes       "Use for distinctive pale ales strongly hopped. Very hard water accentuates the hops flavor. Example: Bass Ale"
                                                                    :ph          "8.0"
                                                                    :sodium      "55.0"
                                                                    :sulfate     "725.0"
                                                                    :version     "1"}}],
                                     :yeasts              [{:yeast {:amount          "0.250"
                                                                    :attenuation     "73.0"
                                                                    :best-for        "Irish Dry Stouts"
                                                                    :flocculation    "Medium"
                                                                    :form            "Liquid"
                                                                    :laboratory      "Wyeast Labs"
                                                                    :max-temperature "22.2"
                                                                    :min-temperature "16.7"
                                                                    :name            "Irish Ale"
                                                                    :notes           "Dry, fruity flavor characteristic of stouts. Full bodied, dry, clean flavor."
                                                                    :product-id      "1084"
                                                                    :type            "Ale"
                                                                    :version         "1"}}]}}]}))))
