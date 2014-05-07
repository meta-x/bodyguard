# bodyguard

ATTN: beta version! use at your own risk!

An opinionated Clojure/Ring library designed for authentication and authorization in web applications and services.

## Example

in `examples/example.clj`

lein ring server

## Installation



## Usage

### dependencies
ring-session
ring-json

### require it
(:require [bodyguard.auth :as bg-auth]
          [bodyguard.utils :as bg-utils])

### define a security policy and optionally an authentication policy
authentication policy: how to determine if the user is authenticated or not
defaults to checking if the `request` object contains a valid session cookie
you can customize this to check against a persistent cookie store or something else (e.g. token based authentication)

security policy: what's the default access to the resources, what resources need to be protected and how should they be protected, what to do in case auth fails
you must define a strategy for your own needs

(def security-policy
  {
    :default-access :anon ; :anon/whitelisting vs :auth/blacklisting - default protection for non-specified routes
    :routes { ; map with routes and required roles; {#"uri" :any} | {:roles #{:admin} :methods #{:post}}
      #"/needs-authentication$" #{:user}
      #"/needs-authorization$" #{:admin}
    }
    :on-authentication-fail (fn [auth-obj] {:status 401 :body "my custom 401 response"})
    :on-authorization-fail (fn [auth-obj required-roles] {:status 403 :body "my custom 403 response"})
  })

### define a 16 byte key to be used in the ciphering of the session cookie
; (this is a ring.middleware.session.cookie config, not bodyguard per se)
; ring.middleware.session.cookie uses AES+CBC+PKCS5Padding to encrypt the data
(def session-cookie-key "16bytekeyforaes!")

### use wrappers
; don't forget that order matters
(def app
  (-> (handler/api app-routes) ; if using compojure
      (bg-auth/wrap-auth-to-params)
      (bg-auth/wrap-authorization security-policy)
      (bg-auth/wrap-authentication security-policy)
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-response)
      (ring-session/wrap-session {:store (ring-session-cookie/cookie-store {:key session-cookie-key})})
  ))

### your application code
set to your auth object to match your security policy using bodyguard's utils functions
`bg-utils/set-current-auth`, `bg-utils/del-current-auth`, `bg-utils/bcrypt-hash`, `bg-utils/bcrypt-verify`

e.g.
in your sign-in/up functions you should return the Set-Cookie header - this is done by using set-current-auth response auth-obj and returning it's value

in your sign-out function you should clean the session by using del-current-auth and returning it's vlaue

## How does it work?
Bodyguard is just a simple library -  a set of functions that glue together in an opinionated way. But since one of the goals is to be flexible enough for anyone to configure to their application's needs, there is a fair amount of manual work.

; /sign/in
validate credentials
create auth obj
create session cookie
cipher with session cookie key
return in response

; /protected/resource
request comes in
unwrapped by wrap-session
wrap-auth-to-params retrieves the auth object that is in the session cookie and adds it in the request params so that they can be easily accessible to your handlers
wrap-authentication checks if the endpoint needs authentication and if the session cookie has the authentication object
wrap-authorization checks if the endpoint needs authorization and if the current user has access


; security policy
I feel like it's better to have a separate security policy from the routing.
It allows for composability and doesn't tie the routing code (compojure, moustache, etc) to the auth library.
I can understand any objections since it promotes a little bit of duplication (changing your routes will force you to remember to change the security policy).
I'm open for suggestions in how to improve this (e.g. some kind of meta-routes) or optional integration to compojure's routes.


## License

Copyright © 2014 Tony Tam and contributors.

Released under the MIT license.
