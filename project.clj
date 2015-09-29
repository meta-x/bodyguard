(defproject bodyguard "0.2.0"
  :description "An opinionated Clojure/Ring library designed for authentication and authorization in web services."
  :url "https://github.com/meta-x/bodyguard"
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.7.0"]
  ]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
)
