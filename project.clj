(defproject com.wallbrew/clj-xml "1.9.0"
  :description "The missing link between clj and xml"
  :url "https://github.com/nnichols/clj-xml"
  :license {:name         "MIT"
            :url          "https://opensource.org/licenses/MIT"
            :distribution :repo
            :comments     "Same-as all Wall-Brew projects"}
  :scm {:name "git"
        :url  "https://github.com/Wall-Brew-Co/clj-xml"}
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [org.clojure/data.xml "0.2.0-alpha9"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "2024.03.13"]
            [com.wallbrew/lein-sealog "1.3.0"]
            [lein-project-version "0.1.0"]
            [mvxcvi/cljstyle "0.16.630"]]
  :deploy-branches ["master"]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.5.3")
