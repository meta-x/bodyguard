(defproject bodyguard "0.2.1"
  :description "An opinionated Clojure/Ring library designed for authentication and authorization in web services."
  :url "https://github.com/meta-x/bodyguard"
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.7.0"]
    [environ "1.0.1"]
    [slingshot "0.12.2"]
    [metosin/ring-http-response "0.6.5"]
    [clj-jwt "0.1.1"]
    [clj-time "0.11.0"]
  ]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
)
