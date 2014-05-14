(ns mx.bodyguard.auth
  (:use [mx.bodyguard.utils])
)

; default authentication policy
(defn- authenticated? [request]
  (not (nil? (get-current-auth request))))

(def default-authentication-policy {:authenticated?-fn authenticated?})

; default responses
(defn- on-authentication-fail [request]
  {:status 401 :body "not authenticated"})
(defn on-authorization-fail [request]
  {:status 403 :body "not authorized"})

(defn wrap-auth-to-params [handler]
  "This middleware adds the session 'auth' object into the request parameters
  for easy access in the handler function."
  (fn [request]
    (handler (assoc-auth-to-params request))
  ))
; TODO:
; wrap-auth-to-params should take an optional user-defined-fn param that allows you a desired args to params
; instead of the hardcoded :auth
; (defn some-fn-defined-by-user [request]
;   returns a map {:a 1 :b 2} (values obtained from the request)
;   )
; the map will be assoc-ed into :params

(defn wrap-authentication
  "Authentication middleware: verifies if the target resource+method is
  protected, if the user is authenticated, the default access policy and acts
  accordingly."
  ([handler sec-policy] (wrap-authentication handler sec-policy default-authentication-policy))
  ([handler sec-policy auth-policy]
  (fn [request]
    (let [target-uri-route (get-route-def (:uri request) (:routes sec-policy))
          is-protected-method (matches-method? (:request-method request) (get-route-methods target-uri-route))
          is-protected-route (not (nil? target-uri-route))
          is-authenticated ((:authenticated?-fn auth-policy) request)
          is-default-access-auth (= (:default-access sec-policy) :auth)]
      (if (and (not is-authenticated) (or (and is-protected-route is-protected-method) is-default-access-auth))
        ((get sec-policy :on-authentication-fail on-authentication-fail) request)
        (handler request))
  ))))

(defn wrap-authorization [handler sec-policy]
  "Authorization middleware: verifies if the user is authorized to access the
  resource+method and acts accordingly."
  (fn [request]
    (let [target-uri-route (get-route-def (:uri request) (:routes sec-policy))
          target-uri-roles (get-route-roles target-uri-route)
          is-protected-method (matches-method? (:request-method request) (get-route-methods target-uri-route))
          current-user-roles (get-current-user-roles request)]
      ; TODO: should :default-access sec-policy be checked here?
      (if (or (not is-protected-method)
              (nil? target-uri-route)
              (is-in-role? current-user-roles target-uri-roles))
        (handler request)
        ((get sec-policy :on-authorization-fail on-authorization-fail) request)
  ))))
