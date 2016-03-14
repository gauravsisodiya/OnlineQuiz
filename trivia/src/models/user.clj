(ns models.user
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [monger.result :refer [ok? has-error?]]
            [clojurewerkz.scrypt.core :as sc]
            [clj-recaptcha.client-v2 :as c]))

(def conn (mg/connect))
(def db (mg/get-db conn "homework1")) ;; database name
(def document "userinfo") ;; document

(defn write-user
  [username password]
  (mc/insert-and-return db document {:username username :password password}))

(defn check-username [username]
  (let [n (Integer. (mc/count db document {:username username}))]
    (if (= n 0)
      true
      false)))

(defn check-captcha [response]
  (:valid? (c/verify "6Lc37g8TAAAAAJ3h_oubXqa1jeAIUaR2nyLJQmz_" response :remote-ip "10.85.47.109")))

(defn check-user [username password]
  (if (not (check-username username))
       (let [users (from-db-object(mc/find-maps db document {:username username}) true)]
         (sc/verify password (:password (get users 0))))
     false)
  )


(defn add-user [username email password]
  (let [h (sc/encrypt password 16384 8 1)]
        (mc/insert-and-return db document {:username username :email email :password h})
    )
  )

(defn read-counter
  []
  (mc/find-maps db document { } ))
