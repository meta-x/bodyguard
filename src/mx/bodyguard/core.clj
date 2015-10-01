(ns mx.bodyguard.core
  (:require [mx.bodyguard.defaults :refer [default-auth-config]]
            [mx.bodyguard.auth :refer :all]))

; TODO: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
; 10.4.2 401 Unauthorized
; The request requires user authentication.
; The response MUST include a WWW-Authenticate header field (section 14.47) containing a challenge applicable to the requested resource.
; The client MAY repeat the request with a suitable Authorization header field (section 14.8).
; If the request already included Authorization credentials, then the 401 response indicates that authorization has been refused for those credentials.
; If the 401 response contains the same challenge as the prior response, and the user agent has already attempted authentication at least once, then the user SHOULD be presented the entity that was given in the response, since that entity might include relevant diagnostic information.
; HTTP access authentication is explained in "HTTP Authentication: Basic and Digest Access Authentication" [43].
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
          (if (is-authenticated? config route-handler request)
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
    (fn [request]
      (let [config (merge default-auth-config config)
            route-handler (fn-get-handler request)
            required-roles (get-route-roles route-handler)
            current-user-roles (get-current-user-roles request)]
        (if (or (empty? required-roles)
                (is-in-role? current-user-roles required-roles))
          (next-handler request)
          ((:on-authorization-fail config) request))))))
