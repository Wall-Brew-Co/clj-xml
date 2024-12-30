(ns clj-xml.samples.junit-test
  "Tests clj-xml against minimal XML data"
  (:require [clj-xml.core :as xml]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [matcher-combinators.test]))

(def parsed-xml
  "Parsed example XML.
       Adapted from: https://raw.githubusercontent.com/testmoapp/junitxml/refs/heads/main/examples/junit-complete.xml"
  (-> "junit.xml"
      io/resource
      io/input-stream
      (xml/xml-source->edn {:include-node? #{:element :characters :comment}
                            :skip-whitespace true
                            :preserve-attrs? true})))

(deftest junit-test
  (testing "XML may be parsed into a sufficiently useful format"
    (is (match? parsed-xml
                {:testsuites-attrs {:name       "Test run"
                                    :tests      "8"
                                    :failures   "1"
                                    :errors     "1"
                                    :skipped    "1"
                                    :assertions "20"
                                    :time       "16.082687"
                                    :timestamp  "2021-04-02T15:48:23"}
                 :testsuites       [{:testsuite       [{:properties       [{:property       [],
                                                                            :property-attrs {:name  "version"
                                                                                             :value "1.774"}}
                                                                           {:property       [],
                                                                            :property-attrs {:name  "commit"
                                                                                             :value "ef7bebf"}}
                                                                           {:property       [],
                                                                            :property-attrs {:name  "browser"
                                                                                             :value "Google Chrome"}}
                                                                           {:property       [],
                                                                            :property-attrs {:name  "ci"
                                                                                             :value "https://github.com/actions/runs/1234"}}
                                                                           {:property       "\n                Config line #1\n                Config line #2\n                Config line #3\n            "
                                                                            :property-attrs {:name "config"}}],
                                                        :properties-attrs {}}
                                                       {:system-out       "Data written to standard out."
                                                        :system-out-attrs {}}
                                                       {:system-err       "Data written to standard error."
                                                        :system-err-attrs {}}
                                                       {:testcase       [],
                                                        :testcase-attrs {:name       "testCase1"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "2"
                                                                         :time       "2.436"
                                                                         :file       "tests/registration.code"
                                                                         :line       "24"}}
                                                       {:testcase       [],
                                                        :testcase-attrs {:name       "testCase2"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "6"
                                                                         :time       "1.534"
                                                                         :file       "tests/registration.code"
                                                                         :line       "62"}}
                                                       {:testcase       [],
                                                        :testcase-attrs {:name       "testCase3"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "3"
                                                                         :time       "0.822"
                                                                         :file       "tests/registration.code"
                                                                         :line       "102"}}
                                                       {:testcase       [{:skipped       [],
                                                                          :skipped-attrs {:message "Test was skipped."}}],
                                                        :testcase-attrs {:name       "testCase4"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "0"
                                                                         :time       "0"
                                                                         :file       "tests/registration.code"
                                                                         :line       "164"}}
                                                       {:testcase       [{:failure       [],
                                                                          :failure-attrs {:message "Expected value did not match."
                                                                                          :type    "AssertionError"}}],
                                                        :testcase-attrs {:name       "testCase5"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "2"
                                                                         :time       "2.902412"
                                                                         :file       "tests/registration.code"
                                                                         :line       "202"}}
                                                       {:testcase       [{:error       [],
                                                                          :error-attrs {:message "Division by zero."
                                                                                        :type    "ArithmeticError"}}],
                                                        :testcase-attrs {:name       "testCase6"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "0"
                                                                         :time       "3.819"
                                                                         :file       "tests/registration.code"
                                                                         :line       "235"}}
                                                       {:testcase       {:system-out       "Data written to standard out."
                                                                         :system-out-attrs {},
                                                                         :system-err       "Data written to standard error."
                                                                         :system-err-attrs {}},
                                                        :testcase-attrs {:name       "testCase7"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "3"
                                                                         :time       "2.944"
                                                                         :file       "tests/registration.code"
                                                                         :line       "287"}}
                                                       {:testcase       [{:properties       [{:property       [],
                                                                                              :property-attrs {:name  "priority"
                                                                                                               :value "high"}}
                                                                                             {:property       [],
                                                                                              :property-attrs {:name  "language"
                                                                                                               :value "english"}}
                                                                                             {:property       [],
                                                                                              :property-attrs {:name  "author"
                                                                                                               :value "Adrian"}}
                                                                                             {:property       [],
                                                                                              :property-attrs {:name  "attachment"
                                                                                                               :value "screenshots/dashboard.png"}}
                                                                                             {:property       [],
                                                                                              :property-attrs {:name  "attachment"
                                                                                                               :value "screenshots/users.png"}}
                                                                                             {:property       "\n                    This text describes the purpose of this test case and provides\n                    an overview of what the test does and how it works.\n                "
                                                                                              :property-attrs {:name "description"}}],
                                                                          :properties-attrs {}}],
                                                        :testcase-attrs {:name       "testCase8"
                                                                         :classname  "Tests.Registration"
                                                                         :assertions "4"
                                                                         :time       "1.625275"
                                                                         :file       "tests/registration.code"
                                                                         :line       "302"}}],
                                     :testsuite-attrs {:errors     "1"
                                                       :tests      "8"
                                                       :name       "Tests.Registration"
                                                       :time       "16.082687"
                                                       :file       "tests/registration.code"
                                                       :skipped    "1"
                                                       :timestamp  "2021-04-02T15:48:23"
                                                       :failures   "1"
                                                       :assertions "20"}}]}))))
