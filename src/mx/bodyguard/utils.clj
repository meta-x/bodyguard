(ns mx.bodyguard.utils
  (:require [clojure.set :as cljset]))

(defn get-route-roles
  "Return the required roles for the resource."
  [handler]
  ; TODO: retrieve roles from the handler's metadata
  )

(defn get-current-user-roles
  "Returns the roles for the current user."
  [request]
  ; TODO: retrieve roles from the jwtoken
  )



(defn route-allows-any-role
  "Does the resource allow access by any authenticated user?"
  [req-roles]
  (= :any req-roles))

(defn is-in-role?
  "Is the user in any of the required roles?"
  [user-roles req-roles]
  (or (route-allows-any-role req-roles)
    (not (empty? (cljset/intersection user-roles req-roles)))))
