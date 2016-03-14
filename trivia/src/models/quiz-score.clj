(ns models.quizscore
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :refer [ok? has-error?]]
            [monger.conversion :refer [from-db-object]]
            ))

(def conn (mg/connect))
(def db (mg/get-db conn "homework1")) ;; database name
(def document "quiz-scores") ;; document
;(def user-document "userinfo")

(defn update-score
  [fname,lname,username,password,sex]
  (mc/insert-and-return db document {:fname fname :lname lname :username username :password password :sex sex }))

(defn write-score
  [username,score]
  (mc/insert-and-return db document {:username username :score score}))

(defn get-score
  [username]
  (from-db-object (mc/find-one-as-map db document {:username username}) true ))

