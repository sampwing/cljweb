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
        (if (= user-password password)
          true
          false)))))

(defn signin
  [request] 
  (let [{query-params :query-params} request]
    (prn query-params)
    (let [{username "username" password "password"} query-params]
      (prn username password)
      (if (true? (authenticate-user username password))
        (response {:status 200
                   :username username})
        (response {:status 401
                   :error "unauthenticated"})))))

(defn signout
  [request]
  (-> (response "session delete")
      (assoc :session nil)))

(defn index 
  [request]
  (prn request)
  (let [{:keys [session cookies query-params]} request]
  (prn query-params)
  (let [count (:count session 0)
        session (assoc session :count (inc count))]
    (-> (response 
          {:vists (str "You've accessed this page " count "times")})
        (assoc :session session)))))

(defn counter-middleware [app]
  (fn [request]
    (let [{session :session} request]
      (let [visits (:visits session 0)
            session (assoc session :visits (int visits))]
        (-> (response
              {:visits (str "You've accessed this page " visits "times.")})
            (assoc :session session))))))
                           

(defn logging-middleware [app] 
  (fn [request]
    (let [{:keys [session cookies query-params]} request]
      (prn (str "Session: " session))
      (prn (str "Cookies: " cookies))
      (prn (str "Request: " request))
      (prn (str "QParams: " query-params)))
    (app request)))

(defn auth
  [username password]
  (let [authenticated (authenticate-user username password)]
  (prn authenticated)
  (if (true? authenticated)
    (response {:status 200})
    (response {:status 401}))))

(defroutes main-routes
  (GET "/" request (index request))
  (POST "/signin" request (signin request))
  (POST "/signin2" [username password] (auth username password))
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
