(ns mx.bodyguard.auth
  (:require [mx.bodyguard.jwt :refer [extract-jwt valid-jwt?]]))

(defn is-protected? [op-mode handler]
  (let [metadata (meta handler)
        protected? (:protected metadata)]
    (if-not (nil? protected?)
      protected?
      (= op-mode :auth))))

(defmulti is-authenticated?
  (fn [config handler request] (:auth-type config)))
(defmethod is-authenticated? :jwt [config handler request]
  ; extract token from request and verify its validity
  (let [token (or (:jwt request) ; if wrap-token is used, then this exists
                  (extract-jwt request))] ; otherwise extract the token
    (valid-jwt? token (:jwt-secret config))))
