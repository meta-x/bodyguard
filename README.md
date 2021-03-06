# bodyguard

An opinionated Clojure/Ring library designed for authentication and authorization in web applications and services.

ATTN: massively revamped; docs to be updated

Please give feedback/suggestions/etc through github issues.


<!--
## Example

run `lein ring server` in `examples/whitney`

This will launch a webserver and open your browser in http://localhost:3000, showing you a series of links that should be self-explanatory.
-->


## Installation

Add

[![Current Version](https://clojars.org/bodyguard/latest-version.svg)](https://clojars.org/bodyguard)

to your leiningen `:dependencies`



## Usage

<!--
### 1. Require it
```clojure
(:require [mx.bodyguard.auth :as bg-auth]
          [mx.bodyguard.utils :as bg-utils])
```

### 2. Define a security policy and (optionally) an authentication policy
The security policy defines what is the default access to the resources (`:default-access`), what resources need to be protected and how should they be protected (`:routes`) and (optionally) what to do in case auth fails (`:on-authentication-fail`/`:on-authorization-fail`). You must define a strategy for your own needs.

```clojure
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

The authentication policy defines how to determine if the user is authenticated or not.
[Defaults](/src/mx/bodyguard/auth.clj#L6?raw=true) to checking if the `request` object contains a valid session cookie. You can/should customize this for a more complete authentication flow.

```clojure
(def authentication-policy
  {
    :authenticated?-fn (fn [] true)
  })
```

### 3. Set a 16 Byte key to be used in the ciphering of the session cookie
(This is a `ring.middleware.session.cookie` config, not `bodyguard` per se)

`ring.middleware.session.cookie` uses AES+CBC+PKCS5Padding to encrypt the data
    (def session-cookie-key "16bytekeyforaes!")

### 4. Use the middleware (don't forget that order matters!)

`wrap-auth-to-params` adds the session 'auth' object into the request parameters for easy access in the handler function. It is an optional middleware. Requires the use of `ring.middleware.nested-params/wrap-nested-params` (automatically included if you're using any of compojure's `handler` functions).

`wrap-authentication` is the authentication middleware. It verifies if the target resource+method is protected, if the user is authenticated, the default access policy and acts accordingly.

`wrap-authorization` is the authorization middleware. Verifies if the user is authorized to access the resource+method and acts accordingly. It is an optional middleware. If you're using this middleware, you MUST use `wrap-authentication`.

```clojure
(def app
  (-> (handler/api app-routes)
      (bg-auth/wrap-authorization security-policy)
      (bg-auth/wrap-authentication security-policy authentication-policy)
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



## Notes

Bodyguard requires a fair amount of manual work since one of the goals is to be flexible enough for anyone to configure it to their application's needs.

### Re: security policy
I feel like it's better to have a separate security policy from the routing.

It allows for composability and doesn't tie the routing code (compojure, moustache, etc) to the auth library.

I can understand any objections since it promotes a little bit of duplication (changing your routes will force you to remember to change the security policy).

I'm open for suggestions in how to improve this (e.g. some kind of meta-routing) or optional integration to compojure's routes.
-->



## License

Copyright © 2014 Tony Tam

Released under the MIT license.
