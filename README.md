# Effect API
Effect API is a library that adds a generic system based on 1.21's enchantment effects, for use within modded datapack content.

## Modules
Effect API is modularised with a core module providing most important things, and other modules extending the core module.

The modules within Effect API are as follows:
- `core`
- `entity`

## Data Structure
The data structure of Effect API generally follows a similar structure to those of Enchantment Effects, 

An example JSON can be found below:
```json
{
  "effects": {
    "effect_api:tick": [
      {
        "effect": {
          "type": "effect_api:enchantment_entity_effect",
          "effect": {
            "type": "minecraft:ignite",
            "duration": 4.0
          }
        },
        "requirements": {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": {
            "stepping_on": {
              "block": {
                "blocks": "minecraft:magma_block"
              }
            }
          }
        }
      }
    ],
    "effect_api:resource": [
      {
        "id": "test:float",
        "resource_type": "effect_api:float",
        "default_value": 5.0
      }
    ]
  }
}
```