# bodyguard

ATTN: beta version! use at your own risk!

An opinionated Clojure/Ring library designed for authentication and authorization in web applications and services.



## Example

run `lein ring server` in `examples/whitney`

This will launch a webserver and open your browser in http://localhost:3000, showing a series of links that should be self-explanatory.



## Installation

Add

    [bodyguard "0.1"]

to your leiningen `:dependencies`



## Usage

### 1. Require it
```
(:require [bodyguard.auth :as bg-auth]
          [bodyguard.utils :as bg-utils])
```

### 2. Define a security policy and (optionally) an authentication policy
The authentication policy defines how to determine if the user is authenticated or not.
[Defaults](https://github.com/meta-x/bodyguard/blob/master/src/bodyguard/auth.clj#L6) to checking if the `request` object contains a valid session cookie. You can/should customize this for a more complete authentication flow.

The security policy defines what is the default access to the resources (`:default-access`), what resources need to be protected and how should they be protected (`:routes`) and (optionally) what to do in case auth fails (`:on-authentication-fail`/`:on-authorization-fail`). You must define a strategy for your own needs.

```
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
```

### 3. Set a 16 Byte key to be used in the ciphering of the session cookie
(This is a ring.middleware.session.cookie config, not bodyguard per se)

`ring.middleware.session.cookie` uses AES+CBC+PKCS5Padding to encrypt the data
    (def session-cookie-key "16bytekeyforaes!")

### 4. Use the middleware (don't forget that order matters!)

`wrap-auth-to-params` adds the session 'auth' object into the request parameters for easy access in the handler function. It is an optional middleware. Requires the use of `ring.middleware.nested-params/wrap-nested-params` (automatically included if you're using any of compojure's `handler` functions).

`wrap-authentication` is the authentication middleware. It verifies if the target resource+method is protected, if the user is authenticated, the default access policy and acts accordingly.

`wrap-authorization` is the authorization middleware. Verifies if the user is authorized to access the resource+method and acts accordingly. It is an optional middleware. If you're using this middleware, you MUST use `wrap-authentication`.

```
(def app
  (-> (handler/api app-routes)
      (bg-auth/wrap-authorization security-policy)
      (bg-auth/wrap-authentication security-policy)
      (bg-auth/wrap-auth-to-params)
      (ring-json/wrap-json-body)
      (ring-json/wrap-json-response)
      (ring-session/wrap-session {:store (ring-session-cookie/cookie-store {:key session-cookie-key})})
  ))
```

### 5. implement your application code
Set your `auth` object to match your security policy using bodyguard's utils functions: `bg-utils/set-current-auth`, `bg-utils/del-current-auth`, `bg-utils/bcrypt-hash`, `bg-utils/bcrypt-verify`

In your sign-in/up handlers you should return a `Set-Cookie` header with the bodyguard session. This is done by using `(set-current-auth response auth-obj)` and returning the new response in your handlers.

In your `sign-out` function you should clean the session by using `del-current-auth` and returning it's value.



## How does it work?
Bodyguard is just a simple library -  a set of functions that glue together in an opinionated way. But since one of the goals is to be flexible enough for anyone to configure it to their application's needs, there is a fair amount of manual work.

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
