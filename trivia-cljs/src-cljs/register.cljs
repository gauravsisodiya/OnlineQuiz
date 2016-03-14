(ns trivia.register
  (:require [goog.net.XhrIo :as xhr]
            [cljs.reader :as reader]
            [domina :as d]
            [domina.events :as events]))


(def eval-username "eval-username")
(def eval-password "eval-password")
(def eval-rpassword "eval-rpassword")
(def eval-button "eval-button")
(def eval-result-u "eval-result_u")
(def eval-result-p "eval-result_p")
(def eval-result-rp "eval-result_rp")

(def url_username "/username_exists")
(def url_password "/password_valid")
(def url_rpassword "/rpassword_valid")

(defn serialize [m] (str m))
(defn de-serialize [s] (reader/read-string s))

(defn receive-username [event]
  (.log js/console (str "user " (.getResponseText (.-target event))))
  (d/set-value! (d/by-id "hid-username")
               (str (.getResponseText (.-target event))))
  (d/set-text! (d/by-id eval-result-u)
               (.getResponseText (.-target event))))

(defn post-for-username [expr-str]
  (xhr/send url_username receive-username "POST" expr-str))

(defn get-username []
  (let [u (.-value (d/by-id eval-username))]
    (.log js/console (str "u: " u))
    (serialize {:username u})))

(defn receive-password [event]
  (.log js/console (str "pass " (.getResponseText (.-target event))))
  (d/set-value! (d/by-id "hid-password")
               (str (.getResponseText (.-target event))))

  (d/set-text! (d/by-id eval-result-p)
               (.getResponseText (.-target event))))

(defn post-for-password [expr-str]
  (xhr/send url_password receive-password "POST" expr-str))

(defn get-password []
  (let [p (.-value (d/by-id eval-password))]
    (.log js/console (str "p: " p))
    (serialize {:password p})))

(defn receive-rpassword [event]
  (.log js/console (str "repass " (.getResponseText (.-target event))))
  (d/set-value! (d/by-id "hid-rpassword")
               (str (.getResponseText (.-target event))))
  (d/set-text! (d/by-id eval-result-rp)
               (.getResponseText (.-target event))))

(defn post-for-rpassword [expr-str]
  (xhr/send url_rpassword receive-rpassword "POST" expr-str))

(defn get-rpassword []
  (let [rp (.-value (d/by-id eval-rpassword))
        p (.-value (d/by-id eval-password))]
    (.log js/console (str "p: " p))
    (serialize {:rpassword rp :password p})))


(defn register-db [event]
    (.log js/console (str "event @ register-db: " event))

  )

(defn post-for-submit []
  (let [username (str (.-value (d/by-id "hid-username")))
        password (str (.-value (d/by-id "hid-password")))
        rpassword (str (.-value (d/by-id "hid-rpassword")))
        u {:username (str (.-value (d/by-id eval-username))) :password (str (.-value (d/by-id eval-password)))}
        user (serialize u)]
     (.log js/console (str "u: " username))
     (.log js/console (str "p: " password))
     (.log js/console (str "r: " rpassword))
      (if (= (str username) "Username avaliable")
              (if (= (str password) "Correct")
                 (if (= (str rpassword) "Password Match")
                      (xhr/send "/register-db" "" "POST" user)
                      (.alert js/window (str "Please Follow Intructions")))
                  (.alert js/window (str "Please Follow Intructions")))
              (.alert js/window (str "Please Follow Intructions"))
    )
  ;(xhr/send url_rpassword receive-rpassword "POST" "")
  ))

(defn ^:export main []
  (events/listen! (d/by-id eval-username)
                  :keyup
                  (fn [event]
                    (post-for-username (get-username))
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (d/by-id eval-password)
                  :keyup
                  (fn [event]
                    (post-for-password (get-password))
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (d/by-id eval-rpassword)
                  :keyup
                  (fn [event]
                    (post-for-rpassword (get-rpassword))
                    (events/stop-propagation event)
                    (events/prevent-default event))))
