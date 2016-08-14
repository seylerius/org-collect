# org-collect

Some folks like using [org files](https://www.orgmode.org) to keep organized, and like to sync this to an android phone for mobile task management. This is complicated when you store your org files in each project directory. This tool solves that problem by syncing each file into a single folder such that syncthing or another such tool can easily sync with your phone. 

## Installation

Clone from this git repository and build with `lein uberjar`, or download a jar from the releases list.

## Usage

    $ java -jar org-collect-0.1.0-standalone.jar -t TARGET_DIRECTORY -d DEFAULT_DIRECTORY OTHER1 OTHER2__

## License

Copyright © 2016 Elliott "Seylerius" Seyler

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
