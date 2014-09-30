(ns web.controller
  (:gen-class))

(use '[ring.util.response :only [response]])

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

(def users {"swing" {:password "123"}
            "nobody" {:password "nobody"}})

(defn get-credentials
  [{session :session {access-key-id "accessKeyId" secret-access-key "secretAccesKey"} :query-params}]
  (if (or (nil? access-key-id) (nil? secret-access-key))
    (json-error 404 "credentials not found")
    (-> (response
         {:session session}))))

(defn store-credentials
  [{{username :username} :session :keys [session query-params]}]
  (if (nil? username)
    (json-error 401 "unauthorized")
    (let [{access-key-id "accessKeyId" secret-access-key "secretAccesKey"} query-params]
    (prn access-key-id)
    (prn secret-access-key)
    (if (or (nil? access-key-id) (nil? secret-access-key))
      (json-error 400 "must provide id and secret")
      (let [session (assoc session :credentials {:access-key-id access-key-id :secret-access-key secret-access-key})]
        (-> (response
             {:session session})))))))

(defn authenticate-user
  [username password]
  (let [user-info (get users username)]
    (if (nil? user-info)
      false
      (let [{user-password :password} user-info]
        (= user-password password)))))

(defn signin
  [{session :session {:keys [username password]} :params :as request}]
  (let [session (assoc session :username username)]
    (if (true? (authenticate-user username password))
      (response {:status 200
                 :visits (str "You've accessed this page " (:count session))})
      (response {:status 401}))))

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
