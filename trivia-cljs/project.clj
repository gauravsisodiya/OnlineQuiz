(defproject trivia-cljs "0.0.1-development"
  :plugins [[lein-cljsbuild "1.0.0"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [domina "1.0.3"]
                 [com.cognitect/transit-cljs "0.8.152"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [clojurewerkz/scrypt "1.2.0"]]
  :cljsbuild
  {:builds
   [{:compiler
     {:output-to "../trivia/resources/public/js/client.js",
      :optimizations :whitespace,
      :pretty-print true},
     :source-paths ["src-cljs"]}]})
