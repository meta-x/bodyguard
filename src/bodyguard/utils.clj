(ns bodyguard.utils
  (:import org.mindrot.jbcrypt.BCrypt)
  (:require [ring.util.response :as ring-rsp]
            [ring.middleware.session :as ring-session]
            [ring.middleware.session.cookie :as ring-session-cookie]
))

; middleware helpers

(defn matches-route? [^String uri ^clojure.lang.MapEntry route]
  ""
  (if (re-find (key route) uri)
    route))

(defn matches-method? [method methods]
  ""
  (or (nil? methods) (not (nil? (some #{method} methods)))))

(defn get-route-def [target-uri routes]
  ""
  (some #(matches-route? target-uri %) routes))

(defn get-route-roles [route-definition]
  ""
  (let [route-vals (second route-definition)]
    (or (:roles route-vals) route-vals)))

(defn get-route-methods [route-definition]
  ""
  (let [route-vals (second route-definition)]
    (:methods route-vals)))

(defn get-resource-roles [request sec-policy]
  ""
  (let [target-uri-route (get-route-def (:uri request) (:routes sec-policy))
        target-uri-roles (get-route-roles target-uri-route)]
    target-uri-roles
  ))

; auth functions

(defn get-current-auth [request]
  ""
  (::bodyguard (:session request)))

(defn set-current-auth [request auth]
  ""
  (let [cur-session (:session request)
        new-session (assoc cur-session ::bodyguard auth)]
    ; returns the request object with the replaced :session object
    (assoc request :session new-session)))

(defn del-current-auth [request]
  ""
  (let [cur-session (:session request)
        new-session (dissoc cur-session ::bodyguard)]
    ; returns the request object with the replaced :session object
    (assoc request :session new-session)))

; user role functions

(defn get-current-user-roles [request]
  ""
  (:roles (get-current-auth request)))

(defn route-allows-any-role [req-roles]
  ""
  (= :any req-roles))

(defn is-in-role? [user-roles req-roles]
  ""
  (or (route-allows-any-role req-roles)
    (not (empty? (clojure.set/intersection user-roles req-roles)))))

; put auth as a request parameter

(defn assoc-auth-to-params [request]
  ""
  (let [auth (get-current-auth request)
        cur-params (:params request)
        new-params (assoc cur-params ::auth auth)]
    (assoc request :params new-params)))

; bcrypt stuff (copied over from cemerick/friend)

(defn bcrypt-hash [password & {:keys [work-factor]}]
  "Hashes a given plaintext password using bcrypt and an optional
   :work-factor (defaults to 10 as of this writing).  Should be used to hash
   passwords included in stored user credentials that are to be later verified
   using `bcrypt-credential-fn`."
  (BCrypt/hashpw password (if work-factor (BCrypt/gensalt work-factor) (BCrypt/gensalt))))

(defn bcrypt-verify [password h]
  "Returns true if the plaintext [password] corresponds to [h]ash,
  the result of previously hashing that password."
  (BCrypt/checkpw password h))
