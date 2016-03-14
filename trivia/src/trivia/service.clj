(ns trivia.service
  (:require [trivia.layout :as layout]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.middleware.params :as middleware]
            [ring.util.response :as ring-resp]
            [models.user :as user]
            [models.userinfo :as userinfo]
            [models.session :as session]
            ;[models.quizscore :as qz-sc]
            [clojurewerkz.scrypt.core :as sc]))

(def q_no "qno1")
(def upper (re-pattern "[A-Z]+"))
(def number (re-pattern "[0-9]+"))
(def special (re-pattern "[\"'!@#$%^&*()?]+"))

(defn login-page [request]
  (layout/render "login.html" {}))

(defn make-map [[k v]]
  {(keyword k) v})

(defn get-form [request]
  (into {} (map make-map (seq (:form-params (middleware/assoc-form-params request "UTF-8"))))))

(defn login-check [request]
  (let [form (get-form request)]
  (if (user/check-captcha (:g-recaptcha-response form))
  (if (user/check-user (:user form) (:password form))
    (do
      (session/add-session (:user form))
      (layout/render "user-home.html" {:title "Online Quiz System"}))
    (layout/render "login.html" {:msg "Incorrect username or password"}))
    (layout/render "login.html" {:msg "Incorrect Captcha"}))))

(defn register-check [request]
  (let [form (get-form request)
        username (:hid-username form)
        password (:hid-password form)
        rpassword (:hid-rpassword form)
        uname (:eval-username form)
        pass (:eval-password form)]
       ; (println username)
      ;  (println password)
       ; (println rpassword)
      ;  (println form)
      (if (user/check-captcha (:g-recaptcha-response form))
          (if (= (str username) "Username avaliable")
              (if (= (str password) "Correct")
                 (if (= (str rpassword) "Password Match")
                   (do
                     (userinfo/write-user (str uname) (sc/encrypt (str pass) 16384 8 1))
                     (layout/render "login.html" {})
                     )
                   (layout/render "register.html" {:msg "Password Didn't Matched Again !!"})
                  )
                (layout/render "register.html" {:msg "Password is not Acceptable !!"}))
            (layout/render "register.html" {:msg "Username already Present"})
          )
         (layout/render "register.html" {:msg "Incorrect Captcha"}))
    ))


(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn selmer-page
  [request]
  (layout/render "example.html" {:title "example"}))

(defn register-page
  [request]
  (layout/render "register.html" {}))

(defn register-db
  [request]
  (let [expr (read-string (slurp (:body request)))]
   ; (println expr)
    (userinfo/write-user (str (:username expr)) (sc/encrypt (str (:password expr)) 16384 8 1))
     (println "Written to DB")
    ;(sc/encrypt (:password form) 16384 8 1)
  (layout/render "login.html" {})
    )

  )


(defn user-home
  [request]
  (if (session/get-session-count)
    (do
  (layout/render "user-home.html" {:title "Online Quiz System"}))
    (layout/render "login.html" {:msg "Please login"})))

(def question {:q "How do you determine if empty?"
               :c ["empty?", "exist?", "map", "reduce"]
               :a "empty?"
               :l "q3"})

(defn get-question
  [request]
  (ring-resp/response (str question)))


(defn get-quiz
  [request]
 ; (println "get quiz")
;  (println (str request))
  (let [expr (read-string (slurp (:body request)))
        qno (str (:btn-value expr))
        quiz (:quiz (userinfo/get-quiz qno))
        ;question (:quiz quiz)
        qz {:qno (str qno) :quiz quiz}]
  ;  (println (str expr))
  ;  (println (str qz))
    (ring-resp/response (str qz))))

(defn add-to-array [element]
  (str (:qno element)))

(defn add-al-to-array [element]
  (str (:quiz_no element)))


(defn get-quizes
  [request]
  (if (session/get-session-count)
    (do
  ;(println "get quiz")
 ; (println (str request))
  (let [quizes (userinfo/get-quizes)
        quiznos (map add-to-array quizes)
        current-user (:user (session/get-session))
        grades (userinfo/get-grades current-user)
        algiven (map add-al-to-array grades)
        disp (clojure.set/difference (set quiznos) (set algiven))
        body {:quizes disp}]
   ; (println (str body))
    (ring-resp/response (str body))))
    (layout/render "login.html" {:msg "Please login"})))

(defn get-grades-map [dict]
  {:quiz_no (:quiz_no dict) :score (:score dict)})

(defn get-grades
  [request]
 ; (println "get grades")
 ; (println (str (userinfo/get-grades)))
  (let [current-user (:user (session/get-session))
        grades (userinfo/get-grades current-user)
;        quiznos (map add-to-array quizes)
       ; g (read-string (grades))
        dbmap (map get-grades-map grades)
        body {:grades dbmap}
        ]
   ; (println current-user)
   ; (println dbmap)
    ;(println grades)
    ;(println (str body))
    (ring-resp/response (str body))))

(defn get-correct-choice [q]
  (let [v (:c q)
        a (:a q)]
    (+ 1 (.indexOf v a))))


(defn check-answer
  [request]
  (try
    (let [expr (read-string (slurp (:body request)))
          quiz_no (:qno expr)
          quiz (:quiz (userinfo/get-quiz (:qno expr)))
          answers (:answers expr)
          ques_no (- (Integer. (:ques_no expr)) 1)
          score (atom 0)
          current-user (:user (session/get-session))]
     ; (println @score)
     ; (println "***********************************")
    ;  (println expr)
      ;(println quiz)

      (loop [x 0]
        (when (< x ques_no)
          (println (str "ans:" (get answers x)))
          (println (str "exp_ans" (get-correct-choice (get quiz x))))

          (if (= (get answers x) (str(get-correct-choice (get quiz x)))) (swap! score inc) )
          (println @score)
          (recur (+ x 1))))
     ; (println (session/get-session))
     ; (println current-user)
    ;  (println (userinfo/get-score "user"))
      ;(userinfo/write-score (str current-user) (into-array [(str (keyword (str "quiz_no") (str quiz_no))) (str (keyword (str "score") (str @score)))]))
      (userinfo/write-score (str current-user) (str quiz_no) (str @score))

      ;(println @score)
      (ring-resp/response (str "Score : " @score)))

    (catch Throwable t
      (str "ERROR: " t))))

(defn strength? [password]
  (not (nil? (and (re-find upper password)
                  (re-find number password)
                  (re-find special password)))))

(defn length? [password]
  (> (count password) 8))

(defn valid-password? [password]
  (and (strength? password) (length? password)))

(defn fetch-content [request]
    (pr-str (read-string (slurp (:body request)))))

(defn pass-check [request]
     (try
       (if (valid-password? (fetch-content request))
         (ring-resp/response (pr-str "Valid Password"))
         (ring-resp/response (pr-str "Invalid password")))
       (catch Throwable t
      (ring-resp/response (str "Enter Password")))))

(defn rpass-check [request]
     (try
       (let [expr (read-string (slurp (:body request)))
        rpass (:rpassword expr)
        pass (:password expr)]
        ; (println rpass)
       ;  (println pass)
    (if (= (str rpass) (str pass))
        (ring-resp/response (str "Password Match"))
        (ring-resp/response (str "Password Doesn't Match"))) )
       (catch Throwable t
      (ring-resp/response (str "Enter Password")))))

(defn username-check [request]
     (try (if (user/check-username (fetch-content request))
       (ring-resp/response (pr-str "Valid username"))
       (ring-resp/response (pr-str "Username already in use")))
       (catch Throwable t
      (ring-resp/response (str "Enter Username")))))

(defn check-password[request]
  (try
    (let [expr (read-string (slurp (:body request)))
      p (String. (:password expr))]
      (if (valid-password? p)
        (ring-resp/response (str "Correct"))
        (ring-resp/response (str "InCorrect"))))
    (catch Throwable t
      (str "ERROR: "t))))

(defn check-user[request]
  (try
    (let [expr (read-string (slurp (:body request)))
          u (String. (:username expr))]
      (if (not= (count(userinfo/get-user u)) 0 )
        (ring-resp/response (str "Username not avaliable"))
        (ring-resp/response (str "Username avaliable"))))
    (catch Throwable t
      (str "ERROR: "t))))

(defn  logout [request]
  (session/logout-session)
  (layout/render "login.html" {:msg "User logged out suuccessfully"}))


(defroutes routes
  [[["/" {:get home-page}
     ["/login" {:get login-page}]
     ["/submit" {:post login-check}]
     ["/register" {:get register-page}]
     ["/register-db" {:post register-check}]
     ["/password_valid" {:post check-password}]
     ["/rpassword_valid" {:post rpass-check}]
     ["/username_exists" {:post check-user}]
     ["/check-answer" {:post check-answer}]
     ["/selmer" {:get selmer-page}]
     ["/user-home" {:get user-home}]
     ["/get-question" {:get get-question}]
     ["/about" {:get about-page}]
     ["/get-quiz" {:post get-quiz}]
     ["/get-quizes" {:post get-quizes}]
     ["/logout" {:get logout}]
     ["/get-grades" {:post get-grades}]]]])

(def service {:env :prod
              ::bootstrap/routes routes
              ::bootstrap/resource-path "/public"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
