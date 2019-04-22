# dativity

[![Clojars Project](https://img.shields.io/clojars/v/dativity.svg)](https://clojars.org/dativity)

Dativity is a stateless, data driven process engine library for Clojure and ClojureScript.

It is inspired by the [Artifact centric business process model.](https://en.wikipedia.org/wiki/Artifact-centric_business_process_model#cite_note-VAN2005-6)

##### Table of Contents  
[Motivation](#motivation)  
[Design](#design)  
[Features](#features)  
[Examples](#examples)  
[Dependencies](#dependencies)  
[License](#license)  

<a name="motivation"/>

## Motivation 

Conventional process engines (like [Activiti](https://www.activiti.org/)) are centered around activities and the sequence in which they should be performed according to design.

The key idea of Dativity is to not say in what sequence actions in a process _should_ be performed. 
But rather what actions are _possible_ to do given how actions depend on information. 

For example, you cannot accept or deny an insurance claim before it has been submitted - it can be reviewed precisely after it has been submitted.

Process software should not keep its own state. The value the software is providing should be accessed through a pure function of two things: a static process-model, and a process instance consisting of data that evolves throughout the instance of the process as information is gathered.

[This blog post](https://morganbentell.wordpress.com/2019/03/18/dativity-the-stateless-process-engine/) offers a more extensive motivation.

<a name="design"/>

## Design

Dativity models a process into three different entities:
* Actions
* Data
* Roles

The above entites relate in the following ways:
* Data (green) can be _required_ by an action
* An action (purple) _produces_ data
* A Role (yellow) _performs_ an action


![](dativity.png)
_a simple credit application process_

In the above example, the action 'create case' produces the data 'case id' and 'customer-id'. When those pieces of information have been added to the case, 'enter-loan-details' can be performed because it only depends on 'case-id' to be present.

<a name="features"/>

## Features

#### Basic functionality
Given a process definition and a set of collected data, Dativity can answer questions like:
* What actions can be performed next?
* What actions can be performed by role X (user, system, officer...)
* What actions have been performed?
* Is action X allowed?
* What data is required for action X?

#### Invalidating 
Sometimes a user goes has to go back and change data. 
Then all subsequent (in the sense that the changed data is required by other actions) actions need to be invalidated.
Dativity has support for this type of scenario, where the case is 'rewinded' to the action that was re-done. Previously entered data is kept, but 'uncommitted', and depending actions need to be performed again.

#### Conditional requirements

<a name="examples"/>

## Examples  

#### Basic functionality

The case data is just a map
```clojure
(def case {})
```

Defining an empty case model
```clojure
(def case-model (dativity.define/empty-case-model))
```

Add action, data and role entities to the model
```clojure
(def case-model
  (-> case-model
      ; Actions
      (dativity.define/add-entity-to-model (dativity.define/action :create-case))
      (dativity.define/add-entity-to-model (dativity.define/action :enter-loan-details))
      (dativity.define/add-entity-to-model (dativity.define/action :produce-credit-application-document))
      (dativity.define/add-entity-to-model (dativity.define/action :sign-credit-application-document))
      (dativity.define/add-entity-to-model (dativity.define/action :payout-loan))
      ; Data entities
      (dativity.define/add-entity-to-model (dativity.define/data :case-id))
      (dativity.define/add-entity-to-model (dativity.define/data :customer-id))
      (dativity.define/add-entity-to-model (dativity.define/data :loan-details))
      (dativity.define/add-entity-to-model (dativity.define/data :credit-application-document))
      (dativity.define/add-entity-to-model (dativity.define/data :applicant-signature))
      (dativity.define/add-entity-to-model (dativity.define/data :officer-signature))
      (dativity.define/add-entity-to-model (dativity.define/data :loan-number))
      ; Roles
      (dativity.define/add-entity-to-model (dativity.define/role :applicant))
      (dativity.define/add-entity-to-model (dativity.define/role :system))
      (dativity.define/add-entity-to-model (dativity.define/role :officer))
      ; Production edges
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :create-case :customer-id))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :create-case :case-id))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :enter-loan-details :loan-details))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :produce-credit-application-document :credit-application-document))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :sign-credit-application-document :applicant-signature))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :sign-credit-application-document :officer-signature))
      (dativity.define/add-relationship-to-model (dativity.define/action-produces :payout-loan :loan-number))
      ; Prerequisite edges
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :enter-loan-details :case-id))
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :produce-credit-application-document :loan-details))
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :produce-credit-application-document :customer-id))
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :sign-credit-application-document :credit-application-document))
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :payout-loan :applicant-signature))
      (dativity.define/add-relationship-to-model (dativity.define/action-requires :payout-loan :officer-signature))
      ; Role-action edges
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :applicant :create-case))
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :applicant :enter-loan-details))
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :applicant :sign-credit-application-document))
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :officer :sign-credit-application-document))
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :system :payout-loan))
      (dativity.define/add-relationship-to-model (dativity.define/role-performs :system :produce-credit-application-document))))
```

Generate a picture of the process definition (requires graphviz, clj only).
```clojure
(dativity.visualize/generate-png case-model)
```

What actions are possible?
```clojure
(dativity.core/next-actions case-model case)
=> #{:create-case}
```

What can the roles do?
```clojure
(dativity.core/next-actions case-model case :applicant)
=> #{:create-case}
(dativity.core/next-actions case-model case :officer)
=> #{}
```

What data is produced by ':create-case'?
```clojure
(dativity.core/data-produced-by-action case-model :create-case)
=> #{:customer-id :case-id}
```

Add some data to the case to simulate a few actions
```clojure
(def case
  (-> case
    (dativity.core/add-data :case-id "542967")
    (dativity.core/add-data :customer-id "199209049345")
    (dativity.core/add-data :loan-details {:amount 100000 :purpose "home"})))
```

What actions were performed (simulated) so far?
```clojure
(dativity.core/actions-performed case-definition case)
=> #{:enter-loan-details :create-case}
```

Who can do what?
```clojure
(dativity.core/next-actions case-model case :applicant)
=> #{}
(dativity.core/next-actions case-model case :system)
=> #{:produce-credit-application-document}
(dativity.core/next-actions case-model case :officer)
=> #{}
```

The document is produced and it is signed by the officer
```clojure
(def case 
  (-> case
    (dativity.core/add-data :credit-application-document {:document-id "abc-123"})))
```    

Who can do what?
```clojure
(dativity.core/next-actions case-model case :applicant)
=> #{}
(dativity.core/next-actions case-model case :system)
=> #{}
(dativity.core/next-actions case-model case :officer)
=> #{:sign-credit-application-document}
```

#### Invalidation

When a user goes 'back' and updates data, it is likely that the 'subsequent' data is no longer valid. For example, if the loan amount is changed, the produced application document is not valid anymore.
In general, when an action is invalidated, all the data that is produced by that action is invalid, and data that is produced by actions that required the invalid data is invalidated recursively.

```clojure
(def case
    (dativity.core/invalidate-action case-model case :enter-loan-details))
```

Now the only available action is to enter loan details again.
```clojure
(dativity.core/next-actions case-model case :applicant)
=> #{:enter-loan-details}
(dativity.core/next-actions case-model case :system)
=> #{}
(dativity.core/next-actions case-model case :officer)
=> #{}
```

Now it's not possible to sign the application. 
```clojure
(dativity.core/action-allowed? case-model case :sign-credit-application-document)
=> false
```

#### Conditionally required data

An action-requires-data edge (arrow in the diagram) can be conditional. The requirement is enforced if and only if a given predicate is true. The predicate is a function of one data node.

To say that applications for loans of more than 300 000 require signatures from two officers we can write

```clojure
(def case-model 
     (dativity.define/add-relationship-to-model case-model
                                                (dativity.define/action-requires-conditional
                                                  :payout-loan
                                                  :counter-signature
                                                  (fn [loan-details]
                                                      (> (:amount loan-details) 300000))
                                                  :loan-details)))
```

<a name="dependencies"/>

## Dependencies
To generate graph pictures, install [graphviz](https://graphviz.gitlab.io/download/):

`brew install graphviz`

The core functionality of Dativity only depends on Clojure.

[Ubergraph](https://github.com/Engelberg/ubergraph) is used as an adapter to graphviz for vizualisation and is only used by a utility namespace.

<a name="license"/>

## License

MIT License

Copyright (c) 2019 Morgan Bentell
