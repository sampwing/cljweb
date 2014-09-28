(ns web.routes
  (:gen-class))

(use '[ring.middleware.session :only [wrap-session]])
(use '[ring.middleware.cookies :only [wrap-cookies]])
(use '[ring.util.response :only [response]])
(require '[compojure.core :refer :all])
(require '[ring.middleware.json :as json])
(require '[ring.adapter.jetty :as jetty])
(require '[compojure.route :as route])
(require '[compojure.handler :as handler])

"https://github.com/ring-clojure/ring/wiki/Getting-Started"

(defn handler [request]
  (prn request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Oh hai"})

(defn json-error
  [error-status message]
  (response 
    {:status error-status
     :body message}))

(def users {"swing" {:password "water"}
            "nobody" {:password "nobody"}})

(defn authenticate-user
  [username password]
  (let [user-info (get users username)]
    (if (nil? user-info)
      false
      (let [{user-password :password} user-info]
        (= user-password password)))))

(defn signout
  [request]
  (-> (response "session delete")
      (assoc :session nil)))

(defn index 
  [request]
  (let [{:keys [session cookies query-params]} request]
  (prn request)
  (prn session)
  (prn cookies)
  (prn query-params)
  (let [count (:count session 0)
        session (assoc session :count (inc count))]
    (-> (response 
        {:vists (str "You've accessed this page " count "times")
         :session session})
        (assoc :session session)))))

(defn logging-middleware [app] 
  (fn [request]
    (let [{:keys [session cookies query-params]} request]
    (app request))))

(defn auth
  [username password]
  (let [authenticated (authenticate-user username password)]
  (if (true? authenticated)
    (response {:status 200
               :session {:username username}})
    (response {:status 401}))))

(defroutes main-routes
  (GET "/" request (index request))
  (POST "/signin" [username password] (auth username password))
  (GET "/signout" request (signout request))
  (route/resources "/info")
  (route/not-found (json-error 404 "Page not found")))

(defn json-api
  [routes]
  (->(handler/api routes)
      wrap-cookies
      wrap-session
      json/wrap-json-params
      json/wrap-json-body
      json/wrap-json-response))

(def app
  (->
    (json-api main-routes)))
