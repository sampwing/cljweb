(ns web.model
  (:gen-class))

(use 'korma.db)
(use 'korma.core)
(require '[clojure.string :as str])

(defdb development
  (sqlite3 {:db "temp.db"}))

(declare users)

(defentity users)

(defn user-authenticate
  [{:keys [username password]}]
    (let [users-found (select users
                              (where {:username username
                                      :password password}))]
      (first users-found)))

