# dativity

Dativity is a stateless, data driven process engine.

Dativity can be used in Clojure and ClojureScript.

Inspired by https://en.wikipedia.org/wiki/Artifact-centric_business_process_model#cite_note-VAN2005-6

## Concept

Conventional process engines are centered around activities and the order in which they should be performed.

The key concept of Dativity is not to say in what sequence actions in a process _should_ be performed. 
But rather what actions _can_ be performed given the data entities that are present.

Dativity models a process into three different entities:
* Actions
* Data
* Roles

The above entites relate in the following ways:
* Data can be _required_ by an action
* An action _produces_ data
* R role _performs_ an action


![](dativity.png)
_a simple credit application process_

In the above example, the action 'create case' produces the data 'case id' and 'customer-id'. When those pieces of information have been added to the case, 'enter-loan-details' can be performed because it only depends on 'case-id' to be present.
##Abilities
Given a process definition and a set of collected data, Dativity can answer questions like:
* What actions can be performed next?
* What actions can be performed by role X (user, system, officer...)
* What actions have been performed?
* Is action X allowed?
* What data is required for action X?

Sometimes a user goes back to a previous step and change data. 
Then all subsequent (in the sense that the changed data is required by other actions) actions need to be invalidated.
Dativity offers an invalidation feature, which rewinds the process to the action that was re-done. Previously entered data is kept, but the depending actions need to be performed again.
  
## Usage

To generate graph pictures, install [graphviz](https://graphviz.gitlab.io/download/):

`brew install graphviz`

## Dependencies
The core functionality of Dativity only depends on Clojure.

[Ubergraph](https://github.com/Engelberg/ubergraph) is used as an adapter to graphviz for vizualisation and is only used by a utility namespace.

## License

MIT License

Copyright (c) 2018 Morgan Bentell
