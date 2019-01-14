[
  // Use https://jolt-demo.appspot.com/#inception to develop/test
  // any changes to this file
  {
    // This section converts the updated json from service-decompostion
    // to org.onap.pomba.common.datatypes.ModelContext
    "operation": "shift",
    "spec": {
      "service-instance-id": "service.uuid",
      "generic-vnfs": {
        "*": {
          "vservers": {
            "*": {
              "vserver-id": "vnfList[&3].vfModuleList[0].vmList[&1].uuid"
            }
          },
          "l3-networks": {
            "*": {
              "network-id": "vnfList[&3].vfModuleList[0].networkList[&1].uuid"
            }
          },
          "vnfcs": {
            "*": {
              "vnfc-id": "vnfList[&3].vnfcList[&1].uuid"
            }
          }
        }
      }
    }
    }
  ]
