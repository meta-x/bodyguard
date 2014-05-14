(defproject bodyguard "0.1b"
  :description "An opinionated Clojure/Ring library designed for authentication and authorization in web applications and services."
  :url "https://github.com/meta-x/bodyguard"
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.mindrot/jbcrypt "0.3m"]
  ]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
)
