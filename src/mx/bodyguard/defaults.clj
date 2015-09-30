(ns mx.bodyguard.defaults
  (:require [environ.core :refer [env]]
            [ring.util.http-response :as status]))

(defn- on-authentication-fail [request]
  (status/unauthorized "not authenticated"))
(defn on-authorization-fail [request]
  (status/forbidden "not authorized"))

(def default-auth-config {
  :operation-mode :auth ; :auth or :anon
  :on-authentication-fail on-authentication-fail
  :on-authorization-fail on-authorization-fail
  :auth-type :jwt
  :jwt-secret (env :jwt-secret)
})
