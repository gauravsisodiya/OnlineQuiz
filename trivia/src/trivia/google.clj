(ns trivia.google
 (:use compojure.core)
 (:require [clj-http.client :as client]
           [cheshire.core :as parse]
           [noir.response :as resp]))

(def CLIENT_ID "your project's client id")
(def REDIRECT_URI "http://localhost:3000/auth_google")
(def login-uri "https://accounts.google.com")
(def CLIENT_SECRET "your project's client secret key")
(def google-user (atom {:google-id "" :google-name "" :google-email ""}))

(def red (str "https://accounts.google.com/o/oauth2/auth?"
              "scope=email%20profile&"
              "redirect_uri=" (ring.util.codec/url-encode REDIRECT_URI) "&"
              "response_type=code&"
              "client_id=" (ring.util.codec/url-encode CLIENT_ID) "&"
              "approval_prompt=force"))

(defroutes google-routes
 (GET "/google" [] (resp/redirect red)))
