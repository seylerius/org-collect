#!/usr/bin/env plk
(ns syncpairs.core
  (:require [planck.shell :refer [sh]]
            [clojure.string :refer [split-lines split]]
            [planck.environ :refer [env]]
            [planck.core :refer [slurp]]
            [planck.io :as io]))

(defn get-sync-pairs []
  (let [pair-file (or (first *command-line-args*) (str "/home/" (:user env) "/.orgsyncpairs"))
        file-text (slurp pair-file)
        pairs (map #(split % #"\|") (split-lines file-text))]
    pairs))

(defn build-sync-command [[one two]]
  (let [comparison (compare (:modified (io/file-attributes one))
                            (:modified (io/file-attributes two)))]
    (cond (= comparison 1) [one two]
          (= comparison -1) [two one])))

(defn sync-pair [[one two]]
  (println (str "Syncing " one " to " two))
  (sh "rsync" "-t" one two))

(defn -main []
  (dorun (map sync-pair (remove nil? (map build-sync-command (get-sync-pairs))))))

(set! *main-cli-fn* -main)
