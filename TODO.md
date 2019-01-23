# TODO

* [DONE] update code and tests to use "committed" data objects
* [DONE] uncommit data objects and dependants
* [DONE] Fully hide ubergraph from consumer
* [DONE] Make compatible with ClojureScript 
    * [DONE] Remove dep to ubergraph - it has dependencies to Java~
    * [DONE] Should use either no library or Loom. Loom does not use Java
        * [DONE] Loom can also visualize with graph viz (only reason to have a library dependeny.)
        * [DONE] If no library is used, it should be possible to load the graph into say Loom and visualize with graphviz. 
* Allow nested data objects
    * Use get-in and assoc-in
    * Maybe use static map that holds the 'path' in the case to the data node
        * Should the static path map be injected or global?
