(ns mx.bodyguard.auth
  (:require [mx.bodyguard.jwt :refer [extract-jwt valid-jwt?]]
            [clojure.set :as cljset]))

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
  (let [token (or (:jwt request)          ; if wrap-token is used, then this exists
                  (extract-jwt request))] ; otherwise extract the token
    ; this is executed again when wrap-valid-token-to-request is used
    ; but that's ok - it's a double check
    (valid-jwt? token (:jwt-secret config))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; roles
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-route-roles
  "Return the required roles for the handler."
  [handler]
  (let [metadata (meta handler)
        roles (:roles metadata)]
    roles))

(defn get-current-user-roles
  "Returns the roles for the current user."
  [request]
  ; ATTN: assumes that roles have been injected into the request by someone else
  ; this is probably a custom middleware that ran before
  (set (:bodgyguard-roles request)))

(defn is-in-role?
  "Is the user in any of the required roles?"
  [user-roles req-roles]
  (or (empty? req-roles)
      (not (empty? (cljset/intersection user-roles req-roles)))))
