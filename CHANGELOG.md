# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [2.0.1] - 2019-08-05
### Fixed
- Bug that broke cljs compilation. [#3](https://github.com/agentbellnorm/dativity/issues/3)

## [2.0.0] - 2019-07-26
### Changed
- Empty seqs are now treated as 'no data'.
- The function `dativity.core/actions-performed-by-role` was renamed to `core/actions-allowed-by-role`
- The function `dativity.define/empty-case-model` was renamed to `dativity.define/empty-process-model`
- Colors and labels are added to the graph in `dativity.visualize` namespace instead of `dativity.define`
- Clojure 1.10.1 is used
- ClojureScript 1.10.520 is used

### Added
- New, less verbose way of defining the model with `dativity.define/create-model`. Old way is still available and used internally.
- New core function `dativity.core/invalidate-data` that should be used instead of `dativity.core/invalidate-action`.
- Using yseras test and error macros

## Deprecated
- `dativity.core/invalidate-action`. Use `dativity.core/invalidate-data` instead.
