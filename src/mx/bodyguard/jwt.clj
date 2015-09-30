(ns mx.bodyguard.jwt
  (:require [slingshot.slingshot :refer [try+]]
            [clj-jwt.core :as jwt]
            [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-long]]))

; TODO: add additional step for ip address validation etc

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; given a claim and a secret, builds the jwtoken
(defn create-jwt [claim secret]
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
        (> right-now exp))
      false)
  (catch Object e
    false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; middleware functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; extracts and decodes the token from the auth header and stores it in the request map
(defn wrap-token [handler error-handler]
  (fn [request]
    (try+
      (->>
        request
        (extract-jwt)
        (assoc request :jwt)
        (handler))
    (catch Object e
      (error-handler handler request e)))))

; this middleware expects wrap-token to have ran before (it adds the :jwt to the request map)
(defn wrap-verify [handler secret on-error]
  (fn [request]
    (if (valid-jwt? (:jwt request) secret)
      (handler request)
      (on-error handler secret request))))

; this middleware extracts data from the jwt-oken's claims and adds it into the request's :params
; useful for acessing the params without having to dig into the jwt
; ATTN: fails silently; assumes token is valid and all ks are present (will select available ks)
; will continue processing if token is not present
(defn wrap-claims-to-params [handler ks on-error]
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