(ns mx.bodyguard.utils
  (:import org.mindrot.jbcrypt.BCrypt))

; middleware helpers

(defn matches-route?
  [^String uri ^clojure.lang.MapEntry route]
  (if (re-find (key route) uri)
    route))

(defn matches-method?
  [method methods]
  (or (nil? methods) (not (nil? (some #{method} methods)))))

(defn get-route-def
  "Find the definition for the target resource in the security policy."
  [target-uri routes]
  (some #(matches-route? target-uri %) routes))

(defn get-route-roles
  "Return the required roles for the resource."
  [route-definition]
  (let [route-vals (second route-definition)]
    (or (:roles route-vals) route-vals)))

(defn get-route-methods
  "Return the allowed methods for the resource."
  [route-definition]
  (let [route-vals (second route-definition)]
    (:methods route-vals)))

(defn get-resource-roles
  "Given the request object and the security policy, returns the required roles for the resource.
  To be externally used - e.g. custom :on-authorization-fail function."
  [request sec-policy]
  (let [target-uri-route (get-route-def (:uri request) (:routes sec-policy))
        target-uri-roles (get-route-roles target-uri-route)]
    target-uri-roles
  ))

; auth functions

(defn get-current-auth
  "Returns the auth object from the request."
  [request]
  (::bodyguard (:session request)))

(defn set-current-auth
  "Sets the auth object into the session."
  [request auth]
  (let [cur-session (:session request)
        new-session (assoc cur-session ::bodyguard auth)]
    ; returns the request object with the replaced :session object
    (assoc request :session new-session)))

(defn del-current-auth
  "Removes the auth object from the session."
  [request]
  (let [cur-session (:session request)
        new-session (dissoc cur-session ::bodyguard)]
    ; returns the request object with the replaced :session object
    (assoc request :session new-session)))

; user role functions

(defn get-current-user-roles
  "Returns the roles for the current user."
  [request]
  (:roles (get-current-auth request)))

(defn route-allows-any-role
  "Does the resource allow access by any authenticated user?"
  [req-roles]
  (= :any req-roles))

(defn is-in-role?
  "Is the user in any of the required roles?"
  [user-roles req-roles]
  (or (route-allows-any-role req-roles)
    (not (empty? (clojure.set/intersection user-roles req-roles)))))

; put auth as a request parameter

(defn assoc-auth-to-params
  "Adds the auth object to the request :params"
  [request]
  (let [auth (get-current-auth request)
        cur-params (:params request)
        new-params (assoc cur-params ::auth auth)]
    (assoc request :params new-params)))

; bcrypt stuff (copied over from cemerick/friend)
; TODO: should probably be removed and moved to it's own crypto ns

(defn bcrypt-hash
  "Hashes a given plaintext password using bcrypt and an optional
   :work-factor (defaults to 10 as of this writing).  Should be used to hash
   passwords included in stored user credentials that are to be later verified
   using `bcrypt-credential-fn`."
  [password & {:keys [work-factor]}]
  (BCrypt/hashpw password (if work-factor (BCrypt/gensalt work-factor) (BCrypt/gensalt))))

(defn bcrypt-verify
  "Returns true if the plaintext [password] corresponds to [h]ash,
  the result of previously hashing that password."
  [password h]
  (BCrypt/checkpw password h))
