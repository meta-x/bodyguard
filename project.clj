(defproject bodyguard "0.1"
  :description "An opinionated Clojure/Ring library designed for authentication and authorization in web applications and services."
  :license {
    :name "Eclipse Public License"
    :url "http://www.eclipse.org/legal/epl-v10.html"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.mindrot/jbcrypt "0.3m"] ; auth
    ; ring
    [ring/ring-core "1.2.2"]
    [ring/ring-jetty-adapter "1.2.2"]
    [ring/ring-servlet "1.2.2"]
    [ring/ring-json "0.3.1"]
  ]
  :profiles {
    :dev {
      :dependencies [
        [compojure "1.1.6"]
        [cheshire "5.2.0"]
      ]
    }
  }
  :plugins [
    [lein-ring "0.8.10"]
  ]
  :ring {:handler bodyguard.test.example/app}
)
