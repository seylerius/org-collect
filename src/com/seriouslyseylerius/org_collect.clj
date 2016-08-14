(ns com.seriouslyseylerius.org-collect
  (:require [me.raynes.fs :as fs]
            [clojure.set :as sets])
  (:gen-class))

(defn select-folders
  "Expand a potentially globbed list of folders"
  [folders]
  (->> folders
       (map (comp fs/glob fs/expand-home))
       (apply concat)
       (filter fs/directory?)))

(defn select-dir-org-files
  "Find all org-files in a given directory"
  [folder]
  (-> folder
      (str "/*.org")
      fs/glob))

(defn get-org-files
  "Select all org-files within specified folders (optionally globbed)"
  [folders]
  (->> folders
      select-folders
      (map select-dir-org-files)
      (apply concat)))

(defn sync-files
  "Sync the provided list of files with the destination folder"
  [source-files dest-folder orphan-folder]
  (println "Got " (count source-files) " source files…")
  (let [dest-files (remove fs/hidden? (fs/list-dir
                                       (fs/expand-home dest-folder)))]
    (println "Got " (count dest-files)" destination files…")
    (let [orphans (sets/difference (set (map fs/base-name dest-files))
                                   (set (map fs/base-name source-files)))]
      (println "Got " (count orphans) " orphans…")
      (doseq [source-file source-files]
        (println "Checking " source-file "…")
        (let [dest-file (str dest-folder "/" (fs/base-name source-file))]
          (let [dest-time (fs/mod-time dest-file)
                source-time (fs/mod-time source-file)]
            (cond (> source-time dest-time)
                  (do
                    (println (str "Copying " source-file
                                  " to " dest-file "…"))
                    (fs/copy source-file dest-file))
                  (< source-time dest-time)
                  (do
                    (println (str "Copying " dest-file
                                  " to " source-file))
                    (fs/copy dest-file source-file))))))
      (doseq [orphan-file (map (partial str dest-folder "/") orphans)]
        (let [orphan-dest (str orphan-folder "/" (fs/base-name orphan-file))]
          (println "Copying " orphan-file " to " orphan-dest)
          (fs/copy orphan-file orphan-dest))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
