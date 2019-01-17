[
  // Use https://jolt-demo.appspot.com/#inception to develop/test
  // any changes to this file
  {
    // This section converts the updated json from service-decomposition
    // to org.onap.pomba.contextbuilder.networkdiscovery.model.NdQuery
    "operation": "shift",
    "spec": {
      "generic-vnfs": {
        "*": {
          "vservers": {
            "*": {
              "vserver-id": "ndQuery[&3].ndResourcesList[0].ndResources[&1].resourceId",
              "#vserver": "ndQuery[&3].ndResourcesList[0].ndResources[&1].resourceType"
            }
          },
          "l3-networks": {
            "*": {
              "network-id": "ndQuery[&3].ndResourcesList[1].ndResources[&1].resourceId",
              "#l3-network": "ndQuery[&3].ndResourcesList[1].ndResources[&1].resourceType"
            }
          },
          "vnfcs": {
            "*": {
              "vnfc-id": "ndQuery[&3].ndResourcesList[2].ndResources[&1].resourceId",
              "#vnfc": "ndQuery[&3].ndResourcesList[2].ndResources[&1].resourceType"
            }
          }
        }
      }
    }
    }

  ]
