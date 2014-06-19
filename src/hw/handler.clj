(ns hw.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
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
           [:input {:type "submit"}]]]]))

(defn greet [params]
  (sql/insert! db :names {:name (:user_name params)})
  (str "Hello, " (:user_name params)))

(defn show-log []
  (let [entries (sql/query db ["select * from names"])]
    (html [:p "Here are people who stopped by:"]
          [:table
           [:tbody
            (map (fn [e] [:tr [:td (:name e)]]) entries)]])))

(defroutes app-routes
  (GET "/" [] (ask-name))
  (POST "/" {params :params} (greet params))
  (GET "/log" []  (show-log))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
