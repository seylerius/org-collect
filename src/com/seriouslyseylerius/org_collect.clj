(ns com.seriouslyseylerius.org-collect
  (:require [me.raynes.fs :as fs]
            [clojure.set :as sets]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(declare cli)

(def cli-options
  [["-t" "–target TARGET" "Target directory"
    :id :target
    :default "~/orgzly"
    :parse-fn #(fs/expand-home %)
    :validate [#(fs/exists? %) "Target directory must exist"]]
   ["-d" "–default DEFAULT" "Default directory"
    :id :default
    :default "~/notes"
    :parse-fn #(fs/expand-home %)
    :validate [#(fs/exists? %) "Default directory must exist"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-q" "nil" "Verbosity level"
    :id :quietness
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] dec))]
   ["-h" "–help"]])

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
  (if (> 0 (-> cli :options :vebosity))
    (println "Got " (count source-files) " source files…"))
  (let [dest-files (remove fs/hidden? (fs/list-dir
                                       (fs/expand-home dest-folder)))]
    (if (> 0 (-> cli :options :vebosity))
      (println "Got " (count dest-files)" destination files…"))
    (let [orphans (sets/difference (set (map fs/base-name dest-files))
                                   (set (map fs/base-name source-files)))]
      (if (> 0 (-> cli :options :vebosity))
        (println "Got " (count orphans) " orphans…"))
      (doseq [source-file source-files]
        (if (> 2 (-> cli :options :vebosity))
          (println "Checking " source-file "…"))
        (let [dest-file (str dest-folder "/" (fs/base-name source-file))]
          (let [dest-time (fs/mod-time dest-file)
                source-time (fs/mod-time source-file)]
            (cond (> source-time dest-time)
                  (do
                    (if (> 1 (-> cli :options :vebosity))
                      (println (str "Copying " source-file
                                    " to " dest-file "…")))
                    (fs/copy source-file dest-file))
                  (< source-time dest-time)
                  (do
                    (if (> 1 (-> cli :options :vebosity))
                      (println (str "Copying " dest-file
                                    " to " source-file)))
                    (fs/copy dest-file source-file))))))
      (doseq [orphan-file (map (partial str dest-folder "/") orphans)]
        (let [orphan-dest (str orphan-folder "/" (fs/base-name orphan-file))]
          (if (> 1 (-> cli :options :vebosity))
            (println "Copying " orphan-file " to " orphan-dest))
          (fs/copy orphan-file orphan-dest))))))

(defn -main
  "Synchronize org files from specified directories with the target folder."
  [& args]
  (println args)
  (def cli (parse-opts args cli-options))
  (sync-files (get-org-files (concat [(-> cli :options :default)] (cli :arguments))) (-> cli :options :target) (-> cli :options :default)))
