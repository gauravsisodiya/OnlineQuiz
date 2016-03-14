(ns trivia.client
  (:require [goog.net.XhrIo :as xhr]
            [cljs.reader :as reader]
            [domina :as d]
            [domina.events :as events]
            [domina.css :as css]))

(def btn-get-question-id "btn-get-question")
(def btn-check-answer-id "btn-check-answer")
(def div-question-id "question-div")
(def div-result-id "result-div")
(def div-quiz-id "quiz-div")
;(def btn-get-quiz1-id "btn-get-quiz1")
;(def btn-get-quiz2-id "btn-get-quiz2")
;(def btn-get-quiz3-id "btn-get-quiz3")
;(def btn-get-quiz4-id "btn-get-quiz4")
(def btn-get-quizes "btn-get-quizes")

(defn serialize [m] (str m))
(defn de-serialize [s] (reader/read-string s))

(defn choice-to-html [choice]
  (str "<li>" choice "</li>"))

(defn choices-to-html [choices]
  (apply str (map choice-to-html choices)))

(defn question-to-html [question]
    (str (:q question) "<br>"
         "<ol>"
         (choices-to-html (:c question))
         "</ol>"

         "<input id=answer-box-"(str(:l question))" type=text /> "
         "<input id=ques_no type=hidden value="(str(:l question)) " /> <br><br>"))

(defn questions-to-html [questions]
  (apply str (map question-to-html questions)))


(defn quiz-to-html [buffer]
  (.log js/console buffer)
  (.log js/console (:quiz (de-serialize buffer)))
  (let [quiz (de-serialize buffer)
        qz (:quiz quiz)
        qno (:qno quiz)]
     (.log js/console (str qno))
     (.log js/console (str "<form> <h1>"(str qno)"</h1>" "<p style=color:red;><i>Note : Please enter the option number</i></p>"
         (questions-to-html qz) "<input id=quiz_no type=hidden value="(str qno) "  /> <input id=ques_no type=hidden value="(str (count qz))" /> <br>  <p> <input id=btn-check-answer type=button value=Get-Score onclick=trivia.client.getscore(); /> </p> </form>"))

    (str "<form> <h1>"(str qno)"</h1>" "<p style=color:red;><i>Note : Please enter the option number</i></p> <h3> <p id=result-div>Score</p></h3>"
         (questions-to-html qz) " <input id=quiz_no type=hidden value="(str qno) "  /> <input id=question_no type=hidden value="(str (count qz))" /> <br>   <p> <input id=btn-check-answer type=button value=Get-Score onclick=trivia.client.getscore(); /> </p> </form>")))

(defn quiz-button-to-html [quiz]
  (str "<button style=color:black; type=button onclick=trivia.client.myalert('"quiz"');>"quiz"</button>&nbsp;&nbsp;"))

(defn quizes-button-to-html [quizes]
  (apply str (map quiz-button-to-html quizes)))

(defn quizes-to-html [buffer]
  (.log js/console buffer)
  (let [buff (de-serialize buffer)
        quizes (:quizes buff)]
       (.log js/console "**************")
       (.log js/console (str buff))

    (str "<form><h2>Select Quizes</h2>"(quizes-button-to-html quizes) " </form>")))

(defn row-to-html [grade]
  (str "<tr><td>"(:quiz_no grade)"</td><td>"(:score grade)"</td></tr>"))

(defn rows-to-html [grades]
  (apply str (map row-to-html grades)))

(defn grades-to-html [buffer]
  (.log js/console buffer)
  (let [buff (de-serialize buffer)
        grades (:grades buff)
        ]
       (.log js/console "**************")
       (.log js/console (str grades))
    (.log js/console (str "<table border=1><tr><th>Quiz Number</th><th>Score</th></tr>"(rows-to-html grades)"</table>" ))

    (str "<table border=1><tr><th>Quiz Number</th><th>Score</th></tr>"(rows-to-html grades)"</table>" )))

(defn receive-question-callback [event]
  (d/set-inner-html! (d/by-id div-question-id)
                     (question-to-html (.getResponseText (.-target event)))))

(defn receive-quiz-callback [event]
  (d/set-inner-html! (d/by-id div-quiz-id)
                     (quiz-to-html (.getResponseText (.-target event)))))

(defn receive-quizes-callback [event]
  (d/set-inner-html! (d/by-id div-quiz-id)
                     (quizes-to-html (.getResponseText (.-target event)))))

(defn receive-grades-callback [event]
    (.log js/console (str "grades callback: "))

  (d/set-inner-html! (d/by-id div-quiz-id)
                     (grades-to-html (.getResponseText (.-target event)))))

(defn receive-result-callback [event]
  (d/set-text! (d/by-id div-result-id)
               (.getResponseText (.-target event))))

(defn get-quiz-id [expr-str]
  (let [b (.-value (d/by-id expr-str))]
    (.log js/console (str "b: " b))
    (serialize {:btn-value b})))

(defn get-question [event]

  (xhr/send "/get-question" receive-question-callback "GET" "")
  (events/stop-propagation event)
  (events/prevent-default event))

(defn get-quiz [event]
       (.log js/console (str "body: " event))
       (let [body (serialize {:btn-value event})]
          (xhr/send "/get-quiz" receive-quiz-callback "POST" (str body))
      ;(xhr/send "/get-quiz" receive-quiz-callback "POST" body)
      ;(events/stop-propagation event)
      ;(events/prevent-default event)
    ))

(defn get-quizes []
         ;(.log js/console (str "body: " body))
      (xhr/send "/get-quizes" receive-quizes-callback "POST" "")
    )

(defn get-grades []
      (.log js/console (str "In get grades " ))
      (xhr/send "/get-grades" receive-grades-callback "POST" "")
    )

(defn get-by-id [x]
 (.-value (d/by-id (str "answer-box-"x))))


(defn check-answer []
  (let [ques_no (+ 1 (js/parseInt (.-value (d/by-id "question_no"))))
        qno (.-value (d/by-id "quiz_no"))
        ans_array (into [] (map get-by-id (vec (range 1 ques_no))))
        b {:answers ans_array, :ques_no (str ques_no), :qno (str qno)}
        body (serialize b)]
        (.log js/console (str "body: " body))
        ;(.log js/console (str "quesno: " ques_no))
        ;(.log js/console (pr-str (vec (range 1 ques_no))))

        ;(.log js/console (pr-str (map get-by-id (vec (range 1 ques_no)))))

    (xhr/send "/check-answer" receive-result-callback "POST" body)))
   ; (events/stop-propagation event)
   ; (events/prevent-default event)))

(defn ^:export myalert [msg]
  (get-quiz msg)
  )

(defn ^:export getscore []
  (check-answer))

(defn ^:export main []
  (events/listen! (d/by-id btn-get-quizes)
                  :click
                  (fn [event]
                    (get-quizes)
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (d/by-id "btn-get-grades")
                  :click
                  (fn [event]
                    (get-grades)
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (css/sel "button")
                  :click
                  (fn [event]
                    (let [atxt (-> event events/current-target d/text)
                           msg  (str "You clicked " atxt)]
                         (.alert js/window msg))
                    (events/stop-propagation event)
                    (events/prevent-default event)))


  )
