(ns bodyguard.core.example
  (:use [compojure.core]
        [cheshire.core]
        [ring.util.response])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as ring-json]
            [ring.middleware.session :as ring-session]
            [ring.middleware.session.cookie :as ring-session-cookie]
            [ring.adapter.jetty :as jetty]
            [bodyguard.auth :as bg-auth]
            [bodyguard.utils :as bg-utils]
))

; helpers

(defn set-auth [auth]
  (let [cur-rsp (response {:auth auth})
        new-rsp (bg-utils/set-current-auth cur-rsp auth)]
    new-rsp))

; web api handlers

(defn sign-in-user []
  (let [auth {:email "user.kevin.costner@bodyguard.something" :roles #{:user}} ; mock auth obj
        response (set-auth auth)]
    response))

(defn sign-in-admin []
  (let [auth {:email "admin.kevin.costner@bodyguard.something" :roles #{:admin :user}} ; mock auth obj
        response (set-auth auth)]
    response))

(defn sign-out [request]
  (let [cur-rsp (response "signed out")
        new-rsp (bg-utils/del-current-auth cur-rsp)]
    new-rsp))

; routes

(defroutes app-routes
  (GET "/" [] (response "<div>hi</div>
                        <a href='/sign/in1'>sign in as user</a><br/>
                        <a href='/sign/in2'>sign in as admin</a><br/>
                        <a href='/sign/out'>sign out</a><br/>
                        <br/>
                        <a href='/public'>public resource - also shows auth obj</a><br/>
                        <a href='/needs-authentication'>protected resource 1 with authentication</a><br/>
                        <a href='/needs-authorization'>protected resource 2 with authorization</a><br/>"))
  (GET "/public" [auth] (response (str "here's an example of accessing the auth obj: " auth)))
  (GET "/needs-authorization" [] (response "yay, you're authorized"))
  (GET "/needs-authentication" [] (response "yay, you're authenticated"))

  (GET "/sign/in1" [] (sign-in-user))
  (GET "/sign/in2" [] (sign-in-admin))
  (GET "/sign/out" request (sign-out request))

  (route/resources "/")
  (route/not-found "Not Found")
)

; auth definition

(def security-policy
  {
    :default-access :anon ; :auth|:anon - default protection for non-specified routes
    :routes { ; map with routes and required roles; {#"uri" :any} | {:roles #{:admin} :methods #{:post}}
      #"/needs-authentication$" #{:user}
      #"/needs-authorization$" #{:admin}
      #"/something" {:roles #{:user} :methods #{:post}} ; only requires user role for POST methods
    }
    :on-authentication-fail (fn [request] {:status 401 :body "my custom 401 response"})
    :on-authorization-fail (fn [request] {:status 403 :body "my custom 403 response"})
  })

; app definition

(def app
  (-> (handler/api app-routes)
      (bg-auth/wrap-authorization security-policy)
      (bg-auth/wrap-authentication security-policy)
      (bg-auth/wrap-auth-to-params)
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-response)
      (ring-session/wrap-session {:store (ring-session-cookie/cookie-store {:key "16bytekeyforaes!"})})
  ))
