(ns models.session
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [monger.result :refer [ok? has-error?]]
            [clojurewerkz.scrypt.core :as sc]
            [clj-recaptcha.client-v2 :as c]))

(def conn (mg/connect))
(def db (mg/get-db conn "homework1")) ;; database name
(def document "session") ;; document

(defn add-session [user]
  (mc/update db document {:session "user"} {:session "user" :user user} {:multi false}))

(defn logout-session []
  (mc/update db document {:session "user"} {:session "user" :user "0"} {:multi false}))

(defn get-session
  []
  (from-db-object (mc/find-one-as-map db document {:session "user"}) true ))

(defn get-session-count []
  (let [n (Integer. (mc/count db document {:session "user" :user "0"}))]
    (if (= n 0)
      true
      false)))
