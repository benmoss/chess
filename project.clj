(defproject forking-chess "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj", "src/cljs", "test/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.6"]
                 [cheshire "5.2.0"]
                 [cljs-http "0.1.3"]
                 [om "0.5.3"]]

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.2"]]
                   :plugins [[lein-cljsbuild "1.0.1"]
                             [com.cemerick/clojurescript.test "0.2.2"]
                             [lein-ring "0.8.7"]
                             [lein-pdo "0.1.1"]
                             [com.cemerick/austin "0.1.3"]]}}

  :aliases {"dev" ["with-profile" "dev" "pdo" "cljsbuild" "auto" "dev," "ring" "server-headless"]}

  :ring {:handler forking-chess.core/app
         :init    forking-chess.core/init}

  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "test/cljs"]
                        :compiler {:output-to "resources/public/js/forking_chess.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/forking_chess.js"
                                   :source-map "resources/public/js/forking_chess.js.map"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :output-wrapper false
                                   :preamble ["react/react.min.js"]
                                   :externs ["om/externs/react.js"]
                                   :closure-warnings
                                   {:non-standard-jsdoc :off}}}
                       {:id "test"
                        :source-paths ["src/cljs" "test/cljs"]
                        :compiler {:output-to "resources/private/js/unit-tests.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]
              :test-commands {"unit" ["phantomjs" :runner
                                      "resources/private/js/unit-tests.js"]}})
