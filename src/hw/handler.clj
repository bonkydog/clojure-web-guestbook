(ns hw.handler
  (:require [compojure.core :refer :all]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [redirect]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clojure.java.jdbc :as sql]
            [hiccup.core :refer :all]))

(def db (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/hw"))

;; heroku addons:add heroku-postgresql:dev
;; heroku pg:psql
;; CREATE TABLE names (id SERIAL, name TEXT);

(defn ask-name []
  (html [:html
         [:head [:title "Hi!"]]
         [:body
          [:p "What is your name?"]
          [:form {:action "/" :method "POST"}
           [:input {:type "text" :name "user_name"}]
           [:input {:type "submit"}]]]
         [:a {:href "/log"} "Guestbook"]]))

(defn record-name [params]
  (sql/insert! db :names {:name (:user_name params)})
  (assoc (redirect "/greet") :session {:user_name (:user_name params)}))

(defn greet [session]
  (html [:p "Hello, " (:user_name session)]
        [:a {:href "/"} "Home"]))

(defn show-log []
  (let [entries (sql/query db ["select * from names"])]
    (html [:p "Here are people who stopped by:"]
          [:table
           [:tbody
            (map (fn [e] [:tr [:td (:name e)]]) entries)]]
          [:a {:href "/"} "Home"])))

(defroutes app-routes
  (GET "/" [] (ask-name))
  (POST "/" {params :params} (record-name params))
  (GET "/greet" {session :session} (greet session))
  (GET "/log" []  (show-log))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-session (handler/site app-routes)))
