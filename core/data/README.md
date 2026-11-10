### Module Graph

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%

graph LR
  subgraph :core
    :core:data["data"]
    :core:common["common"]
    :core:datastore["datastore"]
    :core:model["model"]
    :core:network["network"]
    :core:firebase["firebase"]
  end
  :core:data --> :core:common
  :core:data --> :core:datastore
  :core:data --> :core:model
  :core:data --> :core:network
  :core:data --> :core:firebase
```