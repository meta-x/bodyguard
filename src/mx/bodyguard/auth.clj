(ns mx.bodyguard.auth
  (:require [mx.bodyguard.defaults :refer [default-auth-config]]
            [mx.bodyguard.utils :refer :all]
            [mx.bodyguard.jwt :refer [extract-jwt valid-jwt?]]
))

(defn- is-protected? [op-mode handler]
  (let [metadata (meta handler)
        protected? (:protected metadata)]
    (if-not (nil? protected?)
      protected?
      (= op-mode :auth))))

(defmulti is-authenticated?
  (fn [auth-type handler request] auth-type))

(defmethod is-authenticated? :jwt [handler request]
  ; extract token from request and verify its validity
  (let [token (or (:jwt request) ; if wrap-token is used, then this exists
                  (extract-jwt request))] ; otherwise extract the token
    (valid-jwt? token)))

(defn wrap-authentication
  "Authentication middleware: verifies if the target resource+method is
  protected, if the user is authenticated, the default access policy and acts
  accordingly."
  ([next-handler fn-get-handler]
    (wrap-authentication next-handler fn-get-handler default-auth-config))

  ([next-handler fn-get-handler config]
    (fn [request]
      (let [config (merge default-auth-config config)
            route-handler (fn-get-handler request)]
        (if (is-protected? (:operation-mode config) route-handler)
          ; validate that the user is allowed to access the route-handler
          (if (is-authenticated? (:auth-type) route-handler request)
            (next-handler request)
            ((:on-authentication-fail config) request))
          ; unprotected, continue with the processing
          (next-handler request))))))

(defn wrap-authorization
  "Authorization middleware: verifies if the user is authorized to access the
  resource+method and acts accordingly."
  ([next-handler fn-get-handler]
    (wrap-authentication next-handler fn-get-handler default-auth-config))
  ([next-handler fn-get-handler config]
    ; TODO: tbi
    ; target-uri-roles (get-route-roles target-uri-route)
    ; is-protected?
    ; current-user-roles (get-current-user-roles request)
    ; (if (or (not is-protected-method)
    ;         (nil? target-uri-route)
    ;         (is-in-role? current-user-roles target-uri-roles))
    ;   (handler request)
    ;   ((get sec-policy :on-authorization-fail on-authorization-fail) request))
  ))
