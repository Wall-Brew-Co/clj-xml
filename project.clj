(defproject com.wallbrew/clj-xml "1.7.1"
  :description "The missing link between clj and xml"
  :url "https://github.com/nnichols/clj-xml"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [org.clojure/data.xml "0.2.0-alpha8"]]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.5.3")
