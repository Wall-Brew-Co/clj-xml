(defproject com.wallbrew/clj-xml "1.8.0"
  :description "The missing link between clj and xml"
  :url "https://github.com/nnichols/clj-xml"
  :license {:name         "MIT"
            :url          "https://opensource.org/licenses/MIT"
            :distribution :repo
            :comments     "Same-as all Wall-Brew projects"}
  :scm {:name "git"
        :url  "https://github.com/Wall-Brew-Co/clj-xml"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.xml "0.2.0-alpha8"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.5"]
            [com.wallbrew/lein-sealog "1.0.2"]
            [lein-project-version "0.1.0"]
            [mvxcvi/cljstyle "0.15.0"]]
  :deploy-branches ["master"]
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_pass
                                    :sign-releases false}]]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.5.3")
