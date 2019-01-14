[
  // Use https://jolt-demo.appspot.com/#inception to develop/test
  // any changes to this file
  {
    // This section converts the updated json from service-decompostion
    // to org.onap.pomba.common.datatypes.ModelContext
    "operation": "shift",
    "spec": {
      "generic-vnfs": {
        "*": {
          "vservers": {
            "*": {
              "vserver-id": "ndQuery[&3].ndResourceList[0].ndResource[&1].resourceId",
              "#vserver": "ndQuery[&3].ndResourceList[0].ndResource[&1].resourceType"
            }
          },
          "l3-networks": {
            "*": {
              "network-id": "ndQuery[&3].ndResourceList[1].ndResource[&1].resourceId",
              "#l3-network": "ndQuery[&3].ndResourceList[1].ndResource[&1].resourceType"
            }
          },
          "vnfcs": {
            "*": {
              "vnfc-id": "ndQuery[&3].ndResourceList[2].ndResource[&1].resourceId",
              "#vnfc": "ndQuery[&3].ndResourceList[2].ndResource[&1].resourceType"
            }
          }
        }
      }
    }
    }

  ]
