#!/usr/bin/env plk
(ns detectorgpairs.core
  (:require [clojure.string :refer [split-lines split join]]
            [planck.shell :refer [sh]]
            [planck.core :refer [exit]]
            [planck.io :as io]
            [fipp.edn :refer [pprint]]))

(def org-file #"\.org$")

(defn dir-contents [directory]
  (split-lines (:out (sh "find" directory))))

(defn make-pair [target]
  (let [name (last (split target #"/"))
        syncpoint (str "/home/emhs/orgzly/" name)]
    [syncpoint target]))

(defn make-printable [[syncpoint target]]
  (str syncpoint "|" target))

(defn dups [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
    id))

(defn -main []
  (let [dirs (filter io/directory? *command-line-args*)
        all-files (concat (first (map dir-contents dirs)))
        org-files (filter (partial re-find org-file) all-files)
        filtered-org-files (remove #(re-find #"README|readme|barelabor|opsec/bookmarks|sample|bitmore|SPT-classes" %) org-files)
        pairs (map make-pair filtered-org-files)
        syncpoints (map first pairs)
        printable-pairs (map make-printable pairs)
        output (join "\n" printable-pairs)]
    (if (not (apply distinct? syncpoints))
      (do
        (println "Filenames must be distinct!")
        (pprint (dups syncpoints))
        (exit 1)))
    (println output)))

;; (defn -main []
;;   (pprint (map first (map make-pair (filter (partial re-find org-file) (concat (first (map dir-contents (filter io/directory? *command-line-args*)))))))))

(set! *main-cli-fn* -main)
