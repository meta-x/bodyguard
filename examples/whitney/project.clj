(defproject whitney "0.1"
  :description "An example of using bodyguard."
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [bodyguard "0.1.0-beta3"]
    [ring/ring-core "1.2.2"]
    [ring/ring-json "0.3.1"]
    [compojure "1.1.6"]
    [cheshire "5.2.0"]
  ]
  :plugins [
    [lein-ring "0.8.10"]
  ]
  :ring {:handler whitney.core/app}
)
