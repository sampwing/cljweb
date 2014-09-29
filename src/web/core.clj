(ns web.core
  (:gen-class))

(use '[ring.middleware.session :only [wrap-session]])
(use '[ring.middleware.cookies :only [wrap-cookies]])
(require '[compojure.handler :as handler]) 
(require '[compojure.core :refer :all])
(require '[ring.middleware.json :as json])

(require '[web.routes :as routes])

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
    (json-api routes/main-routes)))
