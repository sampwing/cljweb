(ns web.routes
  (:gen-class))

(require '[compojure.core :refer :all])
(require '[compojure.route :as route])
(require '[web.controller :as controller])

(defroutes main-routes
  (GET "/" request (controller/index request))
  (POST "/credentials" request (controller/store-credentials request))
  (GET "/credentials" request (controller/get-credentials request))
  (POST "/signin" request (controller/signin request))
  (GET "/signout" request (controller/signout request))
  (route/resources "/")
  (route/resources "/credentials")
  (route/resources "/signin")
  (route/resources "/signout")
  (route/not-found (controller/json-error 404 "Page not found")))
