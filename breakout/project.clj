(defproject breakout "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [cljsjs/phaser "2.4.2-0"]
                 [im.chit/purnam.core "0.5.2"]
                 [domina "1.0.3"]
                 [philoskim/debux "0.1.0"]]
  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.4.0"]]
  
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel {:on-jsload "breakout.core/on-js-reload"}
                        :compiler {:main breakout.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true
                                   :optimizations :none
                                   :pretty-print true}}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/app.js"
                                   :main breakout.core
                                   :optimizations :advanced
                                   :pretty-print false} }]})
