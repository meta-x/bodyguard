(ns mx.bodyguard.jwt
  (:require [slingshot.slingshot :refer [try+]]
            [clj-jwt.core :as jwt]
            [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-long]]
            [mx.bodyguard.defaults :refer [default-auth-config]]))

; TODO: add additional step for ip address validation etc

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; given a claim and a secret, builds the jwtoken
(defn create-jwt [secret claim]
  (->
    claim
    (jwt/jwt)
    (jwt/sign :HS256 secret)
    (jwt/to-str)))

; helper that extracts the token from the request map
(defn extract-jwt [request]
  (try+
    (->
      request
      (:headers)
      (get "authorization")
      (clojure.string/replace #"^Bearer\s" "")
      (jwt/str->jwt))
  (catch Object e
    nil)))

; helper that verifies the token and expiry date
(defn valid-jwt? [token secret]
  (try+
    (if (and
          token
          (jwt/verify token secret))
      (let [exp (-> token :claims :exp)
            right-now (to-long (now))]
        (> (* exp 1000) right-now))
      false)
  (catch Object e
    false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; middleware functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; extracts, decodes and validates the token from the auth header
; stores it in the request map if valid; otherwise, continues processing
(defn wrap-valid-token-to-request [handler secret]
  (fn [request]
    (if-let [token (extract-jwt request)]
      (if (valid-jwt? token secret)
        (->>
          token
          (assoc request :jwt)
          (handler))
        (handler request))
      (handler request))))

(defn wrap-token-to-request [handler]
  (fn [request]
    ; TODO: extracts and decodes the token from the auth header
    ; ATTN: does not verify it's validity; for the rare occasions where we want the token and don't care if it's valid or not
  ))

(defn wrap-verify-jwt-to-request [handler config]
  (fn [request]
    ; TODO: assumes token is present in the request as :jwt (i.e. wrap-token-to-request ran before)
    ; verifies if the jwt is valid, calling error-handler if not; continuing otherwise
  ))

; this middleware extracts data from the jwt-oken's claims and adds it into the request's :params
; useful for acessing the params without having to dig into the jwt
; ATTN: fails silently; assumes token is valid and all ks are present (will select available ks)
; will continue processing if token is not present
(defn wrap-claims-to-params [handler ks]
  (fn [request]
    (if-let [token (:jwt request)]
      ; token is present, extract claims, the fields we want, add them to the request and continue
      (let [claims (:claims token)
            cs (select-keys claims ks)]
        (->
          request
          (update-in [:params] merge cs)
          (handler)))
      ; no token, continue processing
      (handler request))))
