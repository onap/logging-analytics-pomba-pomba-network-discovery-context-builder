[
  // Use https://jolt-demo.appspot.com/#inception to develop/test
  // any changes to this file

  {
    // This sections adds nfNamingCode and dataQuality to the json
    // fields returned from service-decomposition that we are
    // interested in
    "operation": "default",
    "spec": {
      "generic-vnfs[]": {
        "*": {
          "vf-modules": {
            "vf-module[]": {
              "*": {}
            }
          },
          "vservers[]": {
            "*": {
              "nfNamingCode": "vserver"
            }
          },
          "l3-networks[]": {
            "*": {
              "type": "l3-network"
            }
          },
          "vnfcList[]": {
            "*": {
              "type": "vnfc"
            }
          }
        }
      }
    }
  },
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
              "vserver-id": "vfList[&3].vfModuleList[0].vmList[&1].uuid",
              "nfNamingCode": "vfList[&3].vfModuleList[0].vmList[&1].nfNamingCode"
            }
          },
          "l3-networks": {
            "*": {
              "network-id": "vfList[&3].vfModuleList[0].networkList[&1].uuid",
              "type": "vfList[&3].vfModuleList[0].networkList[&1].type"
            },
            "vnfcs": {
              "*": {
                "vnfc-id": "vfList[&2].vnfcList[&1].uuid",
                "type": "vfList[&2].vfModuleList[0].type"
              }
            }
          }
        }
      }
    }
  }
  ]
