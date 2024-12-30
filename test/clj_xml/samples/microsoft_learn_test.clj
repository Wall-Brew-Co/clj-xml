(ns clj-xml.samples.microsoft-learn-test
  "Tests clj-xml against minimal XML data"
  (:require [clj-xml.core :as xml]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [matcher-combinators.test]))

(def parsed-xml
  "Parsed example XML.
     Adapted from: https://learn.microsoft.com/en-us/dotnet/standard/linq/sample-xml-file-test-configuration-namespace"
  (-> "microsoft-learn.xml"
      io/resource
      io/input-stream
      (xml/xml-source->edn {:skip-whitespace true
                            :preserve-attrs? true})))

(deftest microsoft-test
  (testing "XML may be parsed into a sufficiently useful format"
    (is (match? parsed-xml
                {:tests-attrs {}
                 :tests       [{:test       {:name              "Convert number to string"
                                             :name-attrs        {},
                                             :commandline       "Examp1.EXE"
                                             :commandline-attrs {},
                                             :input             "1"
                                             :input-attrs       {},
                                             :output            "One"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0001"
                                             :testtype "CMD"}}
                               {:test       {:name              "Find succeeding characters"
                                             :name-attrs        {},
                                             :commandline       "Examp2.EXE"
                                             :commandline-attrs {},
                                             :input             "abc"
                                             :input-attrs       {},
                                             :output            "def"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0002"
                                             :testtype "CMD"}}
                               {:test       {:name              "Convert multiple numbers to strings"
                                             :name-attrs        {},
                                             :commandline       "Examp2.EXE /Verbose"
                                             :commandline-attrs {},
                                             :input             "123"
                                             :input-attrs       {},
                                             :output            "One Two Three"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0003"
                                             :testtype "GUI"}}
                               {:test       {:name              "Find correlated key"
                                             :name-attrs        {},
                                             :commandline       "Examp3.EXE"
                                             :commandline-attrs {},
                                             :input             "a1"
                                             :input-attrs       {},
                                             :output            "b1"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0004"
                                             :testtype "GUI"}}
                               {:test       {:name              "Count characters"
                                             :name-attrs        {},
                                             :commandline       "FinalExamp.EXE"
                                             :commandline-attrs {},
                                             :input             "This is a test"
                                             :input-attrs       {},
                                             :output            "14"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0005"
                                             :testtype "GUI"}}
                               {:test       {:name              "Another Test"
                                             :name-attrs        {},
                                             :commandline       "Examp2.EXE"
                                             :commandline-attrs {},
                                             :input             "Test Input"
                                             :input-attrs       {},
                                             :output            "10"
                                             :output-attrs      {}},
                                :test-attrs {:testid   "0006"
                                             :testtype "GUI"}}]}))))
