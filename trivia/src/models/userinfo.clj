(ns models.userinfo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :refer [ok? has-error?]]
            [monger.conversion :refer [from-db-object]]
            ))

(def conn (mg/connect))
(def db (mg/get-db conn "homework1")) ;; database name
(def document "questions") ;; document
(def user-document "userinfo")
(def score-document "quiz-score")

(defn write-user-info
  [fname,lname,username,password,sex]
  (mc/insert-and-return db user-document {:fname fname :lname lname :username username :password password :sex sex }))

(defn write-user
  [username,password]
  (mc/insert-and-return db user-document {:username username :password password}))


(defn get-user
  [username]
  (from-db-object (mc/find-one-as-map db user-document {:username username}) true ))

(defn get-quiz
  [q_no]
  (from-db-object (mc/find-one-as-map db document {:qno q_no}) true ))

(defn get-quizes
  []
  (mc/find-maps db document))

(defn update-score
  [fname,lname,username,password,sex]
  (mc/insert-and-return db score-document {:fname fname :lname lname :username username :password password :sex sex }))

(defn write-score
  [username,quiz,score]
  (mc/insert-and-return db score-document {:username username :quiz_no quiz :score score}))

(defn get-score
  [username]
  (from-db-object (mc/find-one-as-map db score-document {:username username}) true ))

(defn get-grades
  [username]
  (from-db-object (mc/find-maps db score-document {:username username}) true))
