/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */

package org.onap.pomba.contextbuilder.networkdiscovery.service;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import org.onap.aai.restclient.client.Headers;
import org.onap.pomba.common.datatypes.DataQuality;
import org.onap.pomba.common.datatypes.ModelContext;
import org.onap.pomba.common.datatypes.Network;
import org.onap.pomba.common.datatypes.PInterface;
import org.onap.pomba.common.datatypes.PNF;
import org.onap.pomba.common.datatypes.Pserver;
import org.onap.pomba.common.datatypes.VFModule;
import org.onap.pomba.common.datatypes.VM;
import org.onap.pomba.common.datatypes.VNF;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NdResource;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NdResources;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.onap.pomba.contextbuilder.networkdiscovery.util.RestUtil;
import org.onap.pomba.contextbuilder.networkdiscovery.util.TransformationUtil;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class SpringServiceImpl implements SpringService {
    private static final String ND_TYPE_VSERVER = "vserver";
    private static final String ND_TYPE_L3_NETWORK = "l3-network";
    private static final String ND_TYPE_P_INTERFACE = "p-interface";
    private static Logger log = LoggerFactory.getLogger(RestService.class);

    private static final String APP_NAME = "NetworkDiscoveryContextBuilder";

    private static final String MDC_REQUEST_ID = "RequestId";
    private static final String MDC_SERVER_FQDN = "ServerFQDN";
    private static final String MDC_SERVICE_NAME = "ServiceName";
    private static final String MDC_PARTNER_NAME = "PartnerName";
    private static final String MDC_START_TIME = "StartTime";
    private static final String MDC_SERVICE_INSTANCE_ID = "ServiceInstanceId";
    private static final String MDC_INVOCATION_ID = "InvocationID";
    private static final String MDC_CLIENT_ADDRESS = "ClientAddress";
    private static final String MDC_STATUS_CODE = "StatusCode";
    private static final String MDC_RESPONSE_CODE = "ResponseCode";
    private static final String MDC_INSTANCE_UUID = "InstanceUUID";

    private static final String MDC_RESOURCE_TYPE = "ResourceType";
    private static final String MDC_RESOURCE_ID = "ResourceId";

    private static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME = "X-ONAP-PartnerName";
    private static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID = "X-ONAP-RequestID";
    private static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID = "requestId";
    private static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE = "resourceType";
    private static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID = "resourceId";

    private static UUID instanceUUID = UUID.randomUUID();

    @Autowired
    private String serviceDecompositionBaseUrl;

    @Autowired
    private String networkDiscoveryMicroServiceBasicAuthorization;

    @Autowired
    private String networkDiscoveryCtxBuilderBasicAuthorization;

    @Autowired
    private String serviceDecompositionBasicAuthorization;

    @Autowired
    private String networkDiscoveryMicroServiceBaseUrl;

    @Autowired
    private String networkDiscoveryMicroServiceHostAndPort;

    @Autowired
    private Client jerseyClient;

    @Autowired
    private Client jerseySslClient;

    @Override
    public ModelContext getContext(HttpServletRequest req, String partnerName, String authorization, String requestId,
            String serviceInstanceId, String modelVersionId, String modelInvariantId) throws DiscoveryException {

        String remoteAddress = req.getRemoteAddr() != null ? req.getRemoteAddr() : null;
        initMdc(requestId, partnerName, serviceInstanceId, remoteAddress);

        RestUtil.validateServiceInstanceId(serviceInstanceId);
        RestUtil.validatePartnerName(partnerName);
        validateBasicAuth(authorization);
        String sdReply = getServiceDeomposition(serviceInstanceId, partnerName, requestId);
        ModelContext networkDiscoveryCtx = createModelContextFromSdResonse(sdReply);
        mapPservertoVmModelContext(networkDiscoveryCtx, sdReply);
        NdResources ndResources = createNdResourcesFromSdResonse(sdReply);
        sendNetworkDiscoveryRequest(networkDiscoveryCtx, ndResources, requestId, partnerName);
        return networkDiscoveryCtx;
    }

    /**
     * Validates the Basic authorization header as admin:admin.
     *
     * @throws DiscoveryException
     *             if there is missing parameter
     */
    public void validateBasicAuth(String authorization) throws DiscoveryException {
        if (authorization != null && !authorization.trim().isEmpty() && authorization.startsWith("Basic")) {
            if (!authorization.equals(networkDiscoveryCtxBuilderBasicAuthorization)) {
                throw new DiscoveryException("Authorization Failed", Status.UNAUTHORIZED);
            }
        } else {
            throw new DiscoveryException(
                    "Missing Authorization: " + (authorization == null ? "null" : authorization),
                    Status.UNAUTHORIZED);
        }
    }

    /**
     * Given a service instance ID, GET the resources from Service Decomposition.
     */
    private String getServiceDeomposition(String serviceInstanceId, String partnerName, String requestId)
            throws DiscoveryException {
        if (serviceInstanceId == null) {
            return null;
        }

        String urlStr = serviceDecompositionBaseUrl + "?serviceInstanceId=" + serviceInstanceId;

        log.info("Querying Service Decomposition for service instance {} at url {}", serviceInstanceId, urlStr);

        Response response = jerseyClient.target(urlStr).request().header(Headers.ACCEPT, MediaType.APPLICATION_JSON)
                .header(Headers.AUTHORIZATION, serviceDecompositionBasicAuthorization)
                .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME, partnerName)
                .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID, requestId).get();

        log.info(
                "GET Response from ServiceDecompositionMircoService for serviceInstanceId: {}, status code {}",
                serviceInstanceId, response.getStatusInfo().getStatusCode());

        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            MDC.put(MDC_RESPONSE_CODE, String.valueOf(response.getStatus()));
            MDC.put(MDC_STATUS_CODE, "ERROR");
            throw new DiscoveryException(
                    "Error from Service Decomposition service: " + response.getStatusInfo().getReasonPhrase(),
                    Response.Status.fromStatusCode(response.getStatus()));
        }

        MDC.put(MDC_RESPONSE_CODE, String.valueOf(response.getStatus()));
        MDC.put(MDC_STATUS_CODE, "COMPLETE");
        String reply = response.readEntity(String.class);

        log.info(
                "GET Response from ServiceDecompositionMircoService GetContext for serviceInstanceId: {}, message body:  {}",
                serviceInstanceId, reply);

        return reply;
    }

    private void updateNetworkDiscoveryCtx(ModelContext networkDiscoveryCtx, Map<String, String> resourceMap) {

        for (Network network : networkDiscoveryCtx.getNetworkList()) {
            updateNetworkInstance(resourceMap, network);
        }

        for (VNF vnf : networkDiscoveryCtx.getVnfs()) {
            for (Network network : vnf.getNetworks()) {
                updateNetworkInstance(resourceMap, network);
            }

            for (VFModule vfModule : vnf.getVfModules()) {
                for (VM vm : vfModule.getVms()) {
                    Pserver pserver = vm.getPServer();
                    if (null != pserver) {
                        for (PInterface pInterface : pserver.getPInterfaceList()) {
                            updatePInterface(resourceMap, pInterface);
                        }
                    }
                    updateVmInstance(resourceMap, vm);
                }

                for (Network network : vfModule.getNetworks()) {
                    updateNetworkInstance(resourceMap, network);
                }
            }
        }

        for (PNF pnf : networkDiscoveryCtx.getPnfs()) {
            for (PInterface pInterface : pnf.getPInterfaceList()) {
                updatePInterface(resourceMap, pInterface);
            }
        }
    }

    private void updateVmInstance(Map<String, String> resourceMap, VM vm) {
        String resources = resourceMap.get(vm.getUuid());
        String resultJson = TransformationUtil.transform(resources, ND_TYPE_VSERVER);

        // copy the result into the VM class:
        Gson gson = new Gson();
        VM ndVm = gson.fromJson(resultJson, VM.class);
        vm.setName(ndVm.getName());
        vm.setDataQuality(ndVm.getDataQuality());
        vm.setAttributes(ndVm.getAttributes());
    }

    private void updateNetworkInstance(Map<String, String> resourceMap, Network network) {
        String resources = resourceMap.get(network.getUuid());
        String resultJson = TransformationUtil.transform(resources, ND_TYPE_L3_NETWORK);

        // copy the results into the Network class:
        Gson gson = new Gson();
        Network ndNetwork = gson.fromJson(resultJson, Network.class);
        network.setName(ndNetwork.getName());
        network.setDataQuality(ndNetwork.getDataQuality());
        network.setAttributes(ndNetwork.getAttributes());
    }

    private void updatePInterface(Map<String, String> resourceMap, PInterface pInterface) {
        String resources = resourceMap.get(pInterface.getUuid());
        String resultJson = TransformationUtil.transform(resources, ND_TYPE_P_INTERFACE);

        // copy the results into the Network class:
        Gson gson = new Gson();
        PInterface ndpInterface = gson.fromJson(resultJson, PInterface.class);
        pInterface.setName(ndpInterface.getName());
        pInterface.setDataQuality(ndpInterface.getDataQuality());
        pInterface.setAttributes(ndpInterface.getAttributes());
    }


    /* Return list of requestIds sent to network-discovery microService. */
    private void sendNetworkDiscoveryRequest(ModelContext networkDiscoveryCtx, NdResources ndResources,
            String requestId, String partnerName) throws DiscoveryException {

        Map<String, String> resourceMap = new HashMap<>();

        for (NdResource ndResource : ndResources.getNdResources()) {
            try {
                String resultJson = sendNetworkDiscoveryRequestToSpecificServer(partnerName,
                        requestId, requestId, ndResource.getResourceId(), ndResource.getResourceType());

                resourceMap.put(ndResource.getResourceId(), resultJson);
            } catch (Exception e) {
                log.error("Error from Network Discovery Request - resourceId: {}, message: {}",
                        ndResource.getResourceId(), e.getMessage());

                // Build a fake Network Discovery error result, so it will be returned to the client:
                Resource errorResource = new Resource();
                errorResource.setId(ndResource.getResourceId());
                DataQuality dataQuality = DataQuality.error(e.getMessage());
                errorResource.setDataQuality(dataQuality);
                List<Resource> resourceList = new ArrayList<>();
                resourceList.add(errorResource);
                NetworkDiscoveryNotification ndErrorResult = new NetworkDiscoveryNotification();
                ndErrorResult.setResources(resourceList);
                ndErrorResult.setCode(404);
                Gson gson = new Gson();
                String ndErrorResultToJson = gson.toJson(ndErrorResult);
                resourceMap.put(ndResource.getResourceId(), ndErrorResultToJson);
            }
        }
        updateNetworkDiscoveryCtx(networkDiscoveryCtx, resourceMap);
    }

    private String sendNetworkDiscoveryRequestToSpecificServer(String partnerName,
            String parentRequestId, String requestId, String resourceId, String resourceType)
            throws DiscoveryException {

        // Prepare MDC for logs
        initMdcSendToNetworkDiscoveryMicroService(networkDiscoveryMicroServiceBaseUrl, requestId, resourceType,
                resourceId, partnerName);

        log.info("Network Disvovery request for url {}, resourceId {}, resourceType {}",
                networkDiscoveryMicroServiceBaseUrl, resourceId, resourceType);

        Response response = jerseySslClient.target(networkDiscoveryMicroServiceBaseUrl)
                .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID, requestId)
                .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE, resourceType)
                .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID, resourceId).request()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, networkDiscoveryMicroServiceBasicAuthorization)
                .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME, partnerName)
                .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID, parentRequestId).get();

        log.info("Network Disvovery response status code: {}", response.getStatus());

        MDC.put(MDC_RESPONSE_CODE, String.valueOf(response.getStatus()));
        MDC.put(MDC_STATUS_CODE, "ERROR");

        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            MDC.put(MDC_STATUS_CODE, "ERROR");
            throw new DiscoveryException(
                    "Error from Network Discovery service: " + response.getStatusInfo().getReasonPhrase(),
                    Response.Status.fromStatusCode(response.getStatus()));
        }

        MDC.put(MDC_STATUS_CODE, "SUCCESS");
        String ndResult = response.readEntity(String.class);
        log.info("Message sent. Response ndResult: {}", ndResult);
        return ndResult;
    }


    private void initMdc(String requestId, String partnerName, String serviceInstanceId, String remoteAddress) {
        MDC.clear();
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, APP_NAME);
        MDC.put(MDC_SERVICE_INSTANCE_ID, serviceInstanceId);
        MDC.put(MDC_PARTNER_NAME, partnerName);
        MDC.put(MDC_CLIENT_ADDRESS, remoteAddress);
        MDC.put(MDC_START_TIME, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));
        MDC.put(MDC_INVOCATION_ID, UUID.randomUUID().toString());
        MDC.put(MDC_INSTANCE_UUID, instanceUUID.toString());

        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName());
        } catch (Exception e) {
            // If, for some reason we are unable to get the canonical host name,
            // we
            // just want to leave the field null.
            log.info("Could not get canonical host name for " + MDC_SERVER_FQDN + ", leaving field null");
        }
    }

    private void initMdcSendToNetworkDiscoveryMicroService(String networkDiscoveryUrl, String requestId,
            String resourceType, String resourceId, String partnerName) {

        String parentRequestId = MDC.get(MDC_REQUEST_ID);
        String parentServiceInstanceId = MDC.get(MDC_SERVICE_INSTANCE_ID);
        String parentPartnerName = MDC.get(MDC_PARTNER_NAME);

        MDC.clear();
        initMdc(parentRequestId, parentPartnerName, parentServiceInstanceId, networkDiscoveryMicroServiceHostAndPort);

        MDC.put(MDC_RESOURCE_TYPE, resourceType);
        MDC.put(MDC_RESOURCE_ID, resourceId);
    }

    private ModelContext createModelContextFromSdResonse(String response) {
        List<Object> jsonSpec = JsonUtils.filepathToList("config/jolt/sdToModelContextSpec.json");
        Object jsonInput = JsonUtils.jsonToObject(response);
        Chainr chainr = Chainr.fromSpec(jsonSpec);
        Object transObject = chainr.transform(jsonInput);
        log.debug("Jolt transformed output: {}", JsonUtils.toJsonString(transObject));
        Gson gson = new Gson();
        return gson.fromJson(JsonUtils.toPrettyJsonString(transObject), ModelContext.class);
    }

    private void mapPservertoVmModelContext(ModelContext networkDiscoveryCtx, String ndReply) {
        List<Object> jsonSpec = JsonUtils.filepathToList("config/jolt/pserverToVmSpec.json");
        Object jsonInput = JsonUtils.jsonToObject(ndReply);
        Chainr chainr = Chainr.fromSpec(jsonSpec);
        Object transObject = chainr.transform(jsonInput);
        log.debug("Jolt transformed output: {}", JsonUtils.toJsonString(transObject));
        Gson gson = new Gson();
        JsonObject pserverToVmMap = gson.fromJson(JsonUtils.toPrettyJsonString(transObject), JsonObject.class);
        if (null != pserverToVmMap) {
            JsonArray pserverList = pserverToVmMap.getAsJsonArray("pServer");
            for (JsonElement pserverElement : pserverList) {
                JsonObject pserverObject = pserverElement.getAsJsonObject();
                String vserverId = pserverObject.get("vserver-id").getAsString();
                Pserver pserver = gson.fromJson(pserverElement, Pserver.class);
                for (VNF vnf : networkDiscoveryCtx.getVnfs()) {
                    if (null != vnf) {
                        for (VFModule vfModule :  vnf.getVfModules()) {
                            if (null != vfModule) {
                                for (VM vm : vfModule.getVms()) {
                                    if (vm.getUuid().equals(vserverId)) {
                                        vm.setPServer(pserver);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private NdResources createNdResourcesFromSdResonse(String response) {
        List<Object> jsonSpec = JsonUtils.filepathToList("config/jolt/sdToNdResourcesSpec.json");
        Object jsonInput = JsonUtils.jsonToObject(response);
        Chainr chainr = Chainr.fromSpec(jsonSpec);
        Object transObject = chainr.transform(jsonInput);
        log.debug("Jolt transformed output: {}", JsonUtils.toJsonString(transObject));
        Gson gson = new Gson();
        return gson.fromJson(JsonUtils.toPrettyJsonString(transObject), NdResources.class);
    }

}
