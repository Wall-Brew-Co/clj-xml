(ns clj-xml.core-test
  (:require [clj-xml.core :as sut]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [matcher-combinators.test]))


(defn string->stream
  "Load an Input stream with `s`"
  [^String s]
  (-> s
      .getBytes
      (java.io.ByteArrayInputStream.)))


(def xml-example
  "A sample XML document, as it would appear as parse by `clojure.data.xml`"
  {:tag     :TEST_DOCUMENT
   :attrs   {:XMLNS "https://www.fake.not/real"}
   :content [{:tag     :HEAD
              :attrs   nil
              :content [{:tag     :META_DATA
                         :attrs   {:TYPE "title"}
                         :content ["Some Fake Data!"]}
                        {:tag     :META_DATA
                         :attrs   {:TYPE "tag"}
                         :content ["Example Content"]}]}
             {:tag     :FILE
              :attrs   {:POSTER  "JANE DOE <j.doe@fake-email.not-real>"
                        :DATE    "2020/04/12"
                        :SUBJECT "TEST DATA"}
              :content [{:tag     :GROUPS
                         :attrs   {}
                         :content [{:tag     :GROUP
                                    :attrs   nil
                                    :content ["test-data-club"]}]}
                        {:tag     :SEGMENTS
                         :attrs   nil
                         :content [{:tag     :SEGMENT
                                    :attrs   {:BITS   "00111010"
                                              :NUMBER "58"}
                                    :content ["more data"]}
                                   {:tag     :SEGMENT
                                    :attrs   {:BYTES  "10100010"
                                              :NUMBER "-94"}
                                    :content ["more fake data"]}]}]}]})


(def edn-example
  "An EDN-like map of data"
  {:test-document {:head [{:meta-data "Some Fake Data!"}
                          {:meta-data "Example Content"}]
                   :file {:groups   [{:group "test-data-club"}]
                          :segments [{:segment "more data"}
                                     {:segment "more fake data"}]}}})


(def edn-example-original-keys
  "An EDN-like map of data, with the original XML_CASE keys"
  {:TEST_DOCUMENT {:HEAD [{:META_DATA "Some Fake Data!"}
                          {:META_DATA "Example Content"}]
                   :FILE {:GROUPS   [{:GROUP "test-data-club"}]
                          :SEGMENTS [{:SEGMENT "more data"}
                                     {:SEGMENT "more fake data"}]}}})


(def edn-example-original-keys-force-seq
  "An EDN-like map of data, with the original XML_CASE keys, and forcing children into sequences."
  {:TEST_DOCUMENT [{:HEAD [{:META_DATA "Some Fake Data!"}
                           {:META_DATA "Example Content"}]}
                   {:FILE [{:GROUPS [{:GROUP "test-data-club"}]}
                           {:SEGMENTS [{:SEGMENT "more data"}
                                       {:SEGMENT "more fake data"}]}]}]})


(def edn-example-with-targeted-coercion
  "An EDN-like map of data and forcing some children into sequences."
  {:test-document [{:head [{:meta-data "Some Fake Data!"}
                           {:meta-data "Example Content"}]
                    :file {:groups   [{:group "test-data-club"}]
                           :segments [[{:segment "more data"}]
                                      [{:segment "more fake data"}]]}}]})


(def edn-example-with-attrs
  "An EDN-like map of data with XML attributes."
  {:test-document       {:head       [{:meta-data       "Some Fake Data!"
                                       :meta-data-attrs {:type "title"}}
                                      {:meta-data       "Example Content"
                                       :meta-data-attrs {:type "tag"}}]
                         :file       {:groups       [{:group "test-data-club"}]
                                      :groups-attrs {}
                                      :segments     [{:segment       "more data"
                                                      :segment-attrs {:bits   "00111010"
                                                                      :number "58"}}
                                                     {:segment       "more fake data"
                                                      :segment-attrs {:bytes  "10100010"
                                                                      :number "-94"}}]}
                         :file-attrs {:poster  "JANE DOE <j.doe@fake-email.not-real>"
                                      :date    "2020/04/12"
                                      :subject "TEST DATA"}}
   :test-document-attrs {:xmlns "https://www.fake.not/real"}})


(def edn-example-with-attrs-and-original-keys
  "An EDN-like map of data with XML attributes, and the original XML_CASE keys."
  {:TEST_DOCUMENT       {:HEAD       [{:META_DATA       "Some Fake Data!"
                                       :META_DATA_ATTRS {:TYPE "title"}}
                                      {:META_DATA       "Example Content"
                                       :META_DATA_ATTRS {:TYPE "tag"}}]
                         :FILE       {:GROUPS       [{:GROUP "test-data-club"}]
                                      :GROUPS_ATTRS {}
                                      :SEGMENTS     [{:SEGMENT       "more data"
                                                      :SEGMENT_ATTRS {:BITS   "00111010"
                                                                      :NUMBER "58"}}
                                                     {:SEGMENT       "more fake data"
                                                      :SEGMENT_ATTRS {:BYTES  "10100010"
                                                                      :NUMBER "-94"}}]}
                         :FILE_ATTRS {:POSTER  "JANE DOE <j.doe@fake-email.not-real>"
                                      :DATE    "2020/04/12"
                                      :SUBJECT "TEST DATA"}}
   :TEST_DOCUMENT_ATTRS {:XMLNS "https://www.fake.not/real"}})


(deftest xml->edn-test
  (testing "Functional correctness"
    (is (match? edn-example
                (sut/xml->edn xml-example)))
    (is (match? edn-example-original-keys
                (sut/xml->edn xml-example {:preserve-keys? true})))
    (is (match? edn-example-with-attrs
                (sut/xml->edn xml-example {:preserve-attrs? true})))
    (is (match? (update-in edn-example-with-attrs [:test-document :file] dissoc :groups-attrs)
                (sut/xml->edn xml-example {:preserve-attrs?     true
                                           :remove-empty-attrs? true})))
    (is (match? edn-example-with-attrs-and-original-keys
                (sut/xml->edn xml-example {:preserve-keys?  true
                                           :preserve-attrs? true})))
    (is (match? edn-example-original-keys-force-seq
                (sut/xml->edn xml-example {:preserve-keys? true
                                           :force-seq?     true})))
    (is (nil? (sut/xml->edn nil)))
    (is (nil? (sut/xml->edn :edn)))
    (is (match? {} (sut/xml->edn {})))
    (is (match? []
                (sut/xml->edn [])))
    (is (match? "XML" (sut/xml->edn "XML"))))
  (testing "Limiting eagerness does not impact correctness"
    (is (match? edn-example
                (sut/xml->edn xml-example {:limit-eagerness? true})))
    (is (match? edn-example-original-keys
                (sut/xml->edn xml-example {:preserve-keys?   true
                                           :limit-eagerness? true})))
    (is (match? edn-example-with-attrs
                (sut/xml->edn xml-example {:preserve-attrs?  true
                                           :limit-eagerness? true})))
    (is (match? (update-in edn-example-with-attrs [:test-document :file] dissoc :groups-attrs)
                (sut/xml->edn xml-example {:preserve-attrs?     true
                                           :limit-eagerness?    true
                                           :remove-empty-attrs? true})))
    (is (match? edn-example-with-attrs-and-original-keys
                (sut/xml->edn xml-example {:preserve-keys?   true
                                           :limit-eagerness? true
                                           :preserve-attrs?  true})))
    (is (match? edn-example-original-keys-force-seq
                (sut/xml->edn xml-example {:preserve-keys?   true
                                           :limit-eagerness? true
                                           :force-seq?       true})))
    (is (nil?
          (sut/xml->edn nil {:limit-eagerness? true})))
    (is (nil?
          (sut/xml->edn :edn {:limit-eagerness? true})))
    (is (match? {} (sut/xml->edn {} {:limit-eagerness? true})))
    (is (match? []
                (sut/xml->edn [] {:limit-eagerness? true})))
    (is (match? "XML" (sut/xml->edn "XML" {:limit-eagerness? true})))))


(deftest edn->xml-test
  (testing "Functional correctness"
    (is (match? xml-example
                (-> xml-example
                    (sut/xml->edn {:preserve-attrs? true})
                    (sut/edn->xml {:to-xml-case?      true
                                   :stringify-values? true}))))
    (is (match? xml-example
                (sut/edn->xml edn-example-with-attrs-and-original-keys
                              {:to-xml-case?      true
                               :from-xml-case?    true
                               :stringify-values? true})))
    (is (match? xml-example
                (sut/edn->xml edn-example-with-attrs
                              {:to-xml-case?      true
                               :stringify-values? true})))
    (is (match? [nil]
                (sut/edn->xml nil)))
    (is (nil? (sut/edn->xml :edn)))
    (is (match? {}
                (sut/edn->xml {})))
    (is (match? ["XML"]
                (sut/edn->xml "XML")))
    (is (match? "100"
                (sut/edn->xml 100 {:stringify-values? true})))
    (is (nil? (sut/edn->xml 100))))
  (testing "Limiting eagerness does not impact correctness"
    (is (match? xml-example
                (-> xml-example
                    (sut/xml->edn {:limit-eagerness? true
                                   :preserve-attrs?  true})
                    (sut/edn->xml {:limit-eagerness?  true
                                   :to-xml-case?      true
                                   :stringify-values? true}))))
    (is (match? xml-example
                (sut/edn->xml edn-example-with-attrs-and-original-keys
                              {:to-xml-case?      true
                               :limit-eagerness?  true
                               :from-xml-case?    true
                               :stringify-values? true})))
    (is (match? xml-example
                (sut/edn->xml edn-example-with-attrs
                              {:to-xml-case?      true
                               :limit-eagerness?  true
                               :stringify-values? true})))
    (is (match? [nil]
                (sut/edn->xml nil {:limit-eagerness? true})))
    (is (nil? (sut/edn->xml :edn {:limit-eagerness? true})))
    (is (match? {}
                (sut/edn->xml {} {:limit-eagerness? true})))
    (is (match? ["XML"]
                (sut/edn->xml "XML" {:limit-eagerness? true})))
    (is (match? "100"
                (sut/edn->xml 100 {:stringify-values? true
                                   :limit-eagerness?  true})))
    (is (nil? (sut/edn->xml 100 {:limit-eagerness? true})))))


(def xml-test-string
  "A string of XML data."
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
       "<TEST_DOCUMENT XMLNS=\"https://www.fake.not/real\" XSI=\"abc\">"
       "<HEAD><META_DATA TYPE=\"title\">Some Fake Data!</META_DATA> <META_DATA TYPE=\"tag\">Example Content</META_DATA></HEAD>"
       "<FILE POSTER=\"JANE DOE &lt;j.doe@fake-email.not-real&gt;\" DATE=\"2020/04/12\" SUBJECT=\"TEST DATA\">"
       "<GROUPS><GROUP>test-data-club</GROUP></GROUPS><SEGMENTS><SEGMENT BITS=\"00111010\" NUMBER=\"58\">more data</SEGMENT>"
       "<SEGMENT BYTES=\"10100010\" NUMBER=\"-94\">more fake data</SEGMENT></SEGMENTS></FILE></TEST_DOCUMENT>"))


(deftest xml-string-tests
  (testing "functional correctness"
    ; The formatting of xml-test-string spans multiple lines with spaces for alignment, this is stripped internally
    (is (= (str/replace xml-test-string #"\n   " " ")
           (-> xml-test-string
               string->stream
               (sut/xml-source->edn {:preserve-attrs?  true
                                     :support-dtd      false
                                     :remove-newlines? true})
               (sut/edn->xml-str  {:to-xml-case? true}))
           (-> xml-test-string
               string->stream
               (sut/xml-source->edn {:preserve-attrs?  true
                                     :limit-eagerness? true
                                     :support-dtd      false
                                     :remove-newlines? true})
               (sut/edn->xml-str  {:limit-eagerness? true
                                   :to-xml-case?     true}))
           (-> xml-test-string
               (sut/xml-str->edn {:preserve-attrs?  true
                                  :support-dtd      false
                                  :remove-newlines? true})
               (sut/edn->xml-str  {:to-xml-case? true}))))))


(deftest insignificant-whitespace-test
  (testing "Corner-cases around embedded insignificant whitespace"
    (let [no-ws (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     "<someTag><foo>some text</foo> \n"
                     "</someTag>")
          ws    (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     "<someTag> <foo>some text</foo> \n"
                     "</someTag>")]
      (testing "Against strings"
        (is (match? {:sometag [{:foo "some text"}]}
                    (sut/xml-str->edn no-ws {:skip-whitespace true})))
        (is (match? {:sometag [" " {:foo "some text"} " "]}
                    (sut/xml-str->edn ws {:remove-newlines? true})))
        (is (match? {:sometag [{:foo "some text"}]}
                    (sut/xml-str->edn ws {:skip-whitespace true})))
        (is (match? {:sometag {:foo "some text"
                               :bar "bla"}}
                    (sut/xml-str->edn (str "<sometag><foo>some text</foo><bar>bla</bar></sometag>")
                                      {:skip-whitespace true}))))
      (testing "Against streams"
        (is (match? {:sometag [{:foo "some text"}]}
                    (sut/xml-source->edn (string->stream no-ws) {:skip-whitespace true})))
        (is (match? {:sometag [{:foo "some text"}]}
                    (sut/xml-source->edn (string->stream ws) {:skip-whitespace true})))
        (is (match? {:sometag {:foo "some text"
                               :bar "bla"}}
                    (sut/xml-source->edn (string->stream "<sometag><foo>some text</foo><bar>bla</bar></sometag>")
                                         {:skip-whitespace true})))))))


(deftest force-xml-seq-at-path-test
  (testing "Parsed XML can coerce child nodes to collections"
    (let [nested-data {:a {:b [1 2 3] :c {:d "e"}}}]
      (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/last-child])
                  {:a {:b [1 2 [3]] :c {:d "e"}}}))
      (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/first-child])
                  {:a {:b [[1] 2 3] :c {:d "e"}}}))
      (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/every-child])
                  {:a {:b [[1] [2] [3]] :c {:d "e"}}}))
      (is (match? (sut/force-xml-seq-at-path nested-data [:a :c :d])
                  {:a {:b [1 2 3] :c {:d ["e"]}}}))
      (is (match? (sut/force-xml-seq-at-path nested-data [:a :c :f])
                  {:a {:b [1 2 3] :c {:d "e" :f [nil]}}}))
      (is (thrown-with-msg? IllegalArgumentException
                            #"The key :clj-xml.core/first is incompatible with class clojure.lang.PersistentArrayMap"
            (sut/force-xml-seq-at-path nested-data [sut/first-child])))
      (is (thrown-with-msg? IllegalArgumentException
                            #"The key :c is incompatible with class clojure.lang.PersistentVector"
            (sut/force-xml-seq-at-path nested-data [:a :b :c])))
      (testing "Limiting eagerness does not impact correctness"
        (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/last-child] {:limit-eagerness? true})
                    {:a {:b [1 2 [3]] :c {:d "e"}}}))
        (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/first-child] {:limit-eagerness? true})
                    {:a {:b [[1] 2 3] :c {:d "e"}}}))
        (is (match? (sut/force-xml-seq-at-path nested-data [:a :b sut/every-child] {:limit-eagerness? true})
                    {:a {:b [[1] [2] [3]] :c {:d "e"}}}))
        (is (match? (sut/force-xml-seq-at-path nested-data [:a :c :d] {:limit-eagerness? true})
                    {:a {:b [1 2 3] :c {:d ["e"]}}}))
        (is (match? (sut/force-xml-seq-at-path nested-data [:a :c :f] {:limit-eagerness? true})
                    {:a {:b [1 2 3] :c {:d "e" :f [nil]}}}))
        (is (thrown-with-msg? IllegalArgumentException
                              #"The key :clj-xml.core/first is incompatible with class clojure.lang.PersistentArrayMap"
              (sut/force-xml-seq-at-path nested-data [sut/first-child] {:limit-eagerness? true})))
        (is (thrown-with-msg? IllegalArgumentException
                              #"The key :c is incompatible with class clojure.lang.PersistentVector"
              (sut/force-xml-seq-at-path nested-data [:a :b :c] {:limit-eagerness? true})))))))


(deftest force-xml-seq-at-paths-test
  (testing "Parsed XML can coerce child nodes to collections"
    (let [nested-data {:a {:b [1 2 3]
                           :c {:d "e"}}}]
      (is (match? {:a [{:b [[1] 2 [3]]
                        :c {:d "e"}}]}
                  (sut/force-xml-seq-at-paths nested-data [[:a :b sut/first-child]
                                                           [:a :b sut/last-child]
                                                           [:a]])))
      (is (match? {:a [{:b [[1 2 3]]
                        :c {:d "e"}}]}
                  (sut/force-xml-seq-at-paths nested-data [[:a]
                                                           [:a sut/first-child :b]])))
      (testing "Limiting eagerness does not impact correctness"
        (is (match? {:a [{:b [[1] 2 [3]]
                          :c {:d "e"}}]}
                    (sut/force-xml-seq-at-paths nested-data
                                                [[:a :b sut/first-child]
                                                 [:a :b sut/last-child]
                                                 [:a]]
                                                {:limit-eagerness? true})))
        (is (match? {:a [{:b [[1 2 3]]
                          :c {:d "e"}}]}
                    (sut/force-xml-seq-at-paths nested-data
                                                [[:a]
                                                 [:a sut/first-child :b]]
                                                {:limit-eagerness? true})))))))


(deftest xml-sequence-coercion-test
  (testing "parsed XML can be coerced"
    (is (match? edn-example-with-targeted-coercion
                (sut/xml->edn' xml-example {:force-seq-for-paths [[:test-document :file :segments sut/every-child]
                                                                  [:test-document]]})))
    (is (match? edn-example-with-targeted-coercion
                (sut/xml->edn' xml-example {:limit-eagerness?    true
                                            :force-seq-for-paths [[:test-document :file :segments sut/every-child]
                                                                  [:test-document]]})))))


(deftest xml-seq->edn-test
  (testing "The inner `cond` dispatches as expected"
    (testing "First branch"
      (is (nil? (sut/xml-seq->edn nil)))
      (is (= "xml" (sut/xml-seq->edn "xml"))))
    (testing "Second branch"
      (is (nil? (sut/xml-seq->edn [nil])))
      (is (= "xml" (sut/xml-seq->edn ["xml"]))))
    (testing "Third branch"
      (is (= {:hello   "World"
              :goodbye "Space"}
             (sut/xml-seq->edn [{:tag     "Hello"
                                 :content "World"}
                                {:tag     "Goodbye"
                                 :content "Space"}]))))
    (testing "Fourth branch"
      (is (= {}
             (sut/xml-seq->edn {}))))
    (testing "Fifth branch"
      (is (= {:hello "Space"}
             (sut/xml-seq->edn {:tag     "Hello"
                                :content "Space"}))))
    (testing "Sixth branch"
      (is (match? [{:hello "World"}
                   {:hello "Space"}]
                  (sut/xml-seq->edn [{:tag     "Hello"
                                      :content "World"}
                                     {:tag     "Hello"
                                      :content "Space"}]))))
    (testing "Seventh branch"
      (is (= "12345"
             (sut/xml-seq->edn 12345 {:stringify-values? true}))))

    (testing "Default branch"
      (is (= 12345
             (sut/xml-seq->edn 12345)))))
  (testing "The inner `cond` dispatches as expected without eagerness"
    (testing "First branch"
      (is (nil? (sut/xml-seq->edn nil {:limit-eagerness? true})))
      (is (= "xml" (sut/xml-seq->edn "xml" {:limit-eagerness? true}))))
    (testing "Second branch"
      (is (nil? (sut/xml-seq->edn [nil] {:limit-eagerness? true})))
      (is (= "xml" (sut/xml-seq->edn ["xml"] {:limit-eagerness? true}))))
    (testing "Third branch"
      (is (= {:hello   "World"
              :goodbye "Space"}
             (sut/xml-seq->edn [{:tag     "Hello"
                                 :content "World"}
                                {:tag     "Goodbye"
                                 :content "Space"}]
                               {:limit-eagerness? true}))))
    (testing "Fourth branch"
      (is (= {}
             (sut/xml-seq->edn {} {:limit-eagerness? true}))))
    (testing "Fifth branch"
      (is (= {:hello "Space"}
             (sut/xml-seq->edn {:tag     "Hello"
                                :content "Space"}
                               {:limit-eagerness? true}))))
    (testing "Sixth branch"
      (is (match? [{:hello "World"}
                   {:hello "Space"}]
                  (sut/xml-seq->edn [{:tag     "Hello"
                                      :content "World"}
                                     {:tag     "Hello"
                                      :content "Space"}]
                                    {:limit-eagerness? true}))))
    (testing "Seventh branch"
      (is (= "12345"
             (sut/xml-seq->edn 12345 {:stringify-values? true
                                      :limit-eagerness?  true}))))

    (testing "Default branch"
      (is (= 12345
             (sut/xml-seq->edn 12345 {:limit-eagerness? true}))))))
