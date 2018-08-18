#!/usr/bin/env plk
(ns syncpairs.core
  (:require [planck.shell :refer [sh]]
            [clojure.string :refer [split-lines split]]
            [planck.environ :refer [env]]
            [planck.core :refer [slurp]]
            [planck.io :as io])

(defn get-sync-pairs []
  (let [pair-file (or (first *command-line-args*) (str "/home/" (:user env) "/.orgsyncpairs"))
        file-text (slurp pair-file)
        pairs (map #(split % #"\|") (split-lines file-text))]
    pairs))

(defn build-sync-command [[one two]]
  (let [comparison (compare (:modified (io/file-attributes one))
                            (:modified (io/fileattributes two)))]
    (cond (= comparison 1) ["rsync" "-t" one two]
          (= comparison -1) ["rsync" "-t" two one])))

(defn -main
  (println "Stub"))

(set! *main-cli-fn* -main)
