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
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.onap.pomba.common.datatypes.Attribute;
import org.onap.pomba.common.datatypes.ModelContext;
import org.onap.pomba.common.datatypes.Network;
import org.onap.pomba.common.datatypes.VF;
import org.onap.pomba.common.datatypes.VFModule;
import org.onap.pomba.common.datatypes.VM;
import org.onap.pomba.common.datatypes.VNFC;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NetworkDiscoveryRspInfo;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.onap.pomba.contextbuilder.networkdiscovery.util.RestUtil;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class SpringServiceImpl implements SpringService {
    private static Logger log = LoggerFactory.getLogger(RestService.class);
    public static final String APP_NAME = "NetworkDiscoveryContextBuilder";

    public static final String MDC_REQUEST_ID = "RequestId";
    public static final String MDC_SERVER_FQDN = "ServerFQDN";
    public static final String MDC_SERVICE_NAME = "ServiceName";
    public static final String MDC_PARTNER_NAME = "PartnerName";
    public static final String MDC_START_TIME = "StartTime";
    public static final String MDC_SERVICE_INSTANCE_ID = "ServiceInstanceId";
    public static final String MDC_INVOCATION_ID = "InvocationID";
    public static final String MDC_CLIENT_ADDRESS = "ClientAddress";
    public static final String MDC_STATUS_CODE = "StatusCode";
    public static final String MDC_RESPONSE_CODE = "ResponseCode";
    public static final String MDC_INSTANCE_UUID = "InstanceUUID";

    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_APP = "NetworkDiscoveryContextBuilder_TO_NetworkDiscoveryMicroService";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_MSG_NAME = "MsgName";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_FINDBYRESOURCEIDANDTYPE = "findbyResourceIdAndType";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_URL = "CallingURL";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_REQUEST_ID = "ChildRequestId";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_RESOURCE_TYPE = "ResourceType";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_RESOURCE_ID = "ResourceID";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_CALL_BACK_URL = "CallbackUrl";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS = "Status";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_NO_MORE_CALL_BACK = "NoMoreCallBack";
    public static final String MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_WAIT_FOR_NOTIFICATION_TIME_OUT = "NotificationTimeOut";

    public static final String NETWORK_DISCOVERY_RSP_STATE_REQUEST_SENT = "RequestSent";
    public static final String NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED = "AllRspReceived";
    public static final String NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER = "___";
    public static final String NETWORK_DISCOVERY_CTX_BUILDER_NETWORK_DISCOVERY_NOTIFICATION_PATH = "/network-discovery/service/networkDiscoveryNotification";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME = "X-ONAP-PartnerName";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID = "X-ONAP-RequestID";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID = "requestId";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE = "resourceType";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID = "resourceId";
    public static final String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_NOTIFICATION_URL = "notificationURL";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_APP = "NetworkDiscoveryMicroService_TO_NetworkDiscoveryContextBuilder";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_MSG_NAME = "MsgName";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_NETWORKDISCOVERYNOTIFICATION = "NetworkDiscoveryNotification";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_REQUEST_ID = "RequestId";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS = "Status";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_UNKNOWN_REQ = "EntryRemoved_dueTo_timeOut_or_error_or_neverExisit";
    public static final String MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_SUCCESS = "SUCCESS";

    private static UUID instanceUUID = UUID.randomUUID();
    private static Map<String, NetworkDiscoveryRspInfo> networkDiscoveryInfoList = new HashMap<>();
    private static final AtomicLong uniqueSeq = new AtomicLong();

    private class NdResource {

        private String resourceType;
        private String resourceId;

        public NdResource(String type, String id) {
            this.resourceType = type;
            this.resourceId = id;        };

        public String getResourceType() {
            return this.resourceType;
        }
        public String getResourceId() {
            return this.resourceId;
        }
    }


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
    private String networkDiscoveryCtxBuilderBaseUrl;

    @Autowired
    private long networkDiscoveryResponseTimeOutInMilliseconds;

    @Autowired
    private String networkDiscoveryMicroServiceHostAndPort;

    @Autowired
    private Client jerseyClient;

    @javax.annotation.Resource
    private Map<String, String> networkDiscoveryCtxBuilderResourceTypeMapping;

    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public ModelContext getContext(HttpServletRequest req, String partnerName, String authorization, String requestId,
            String serviceInstanceId, String modelVersionId, String modelInvariantId) throws DiscoveryException {

        String remoteAddress = req.getRemoteAddr() != null ? req.getRemoteAddr() : null;
        initMDC(requestId, partnerName, serviceInstanceId, remoteAddress);

        try {
            RestUtil.validateServiceInstanceId(serviceInstanceId);
            RestUtil.validatePartnerName(partnerName);
            validateBasicAuth(authorization);
            ModelContext networkDiscoveryCtx = getServiceDeomposition(serviceInstanceId, partnerName, requestId);

            CountDownLatch latchSignal = createCountDownLatch(networkDiscoveryCtx);

            if (latchSignal == null) {
                // Nothing to send
                return networkDiscoveryCtx;
            }

            List<String> sentRequestIdList = sendNetworkDiscoveryRequest(networkDiscoveryCtx, serviceInstanceId, requestId,
                    partnerName, latchSignal);

            int numOfMsgSent = sentRequestIdList.size();
            if ((numOfMsgSent > 0) && (latchSignal != null)) {
                // The main task waits for four threads
                if (false == latchSignal.await(networkDiscoveryResponseTimeOutInMilliseconds, TimeUnit.MILLISECONDS)) {
                    // When it comes here, it is due to time out.
                    log.info("Wait for Latch Signal time out " + serviceInstanceId);
                }
                return updateServiceDecompCtx(networkDiscoveryCtx, sentRequestIdList);
            } else {
                return networkDiscoveryCtx;
            }

        } catch (Exception x) {
            DiscoveryException exception = new DiscoveryException(x.getMessage(), x);
            MDC.put(MDC_RESPONSE_CODE, String.valueOf(exception.getHttpStatus().getStatusCode()));
            MDC.put(MDC_STATUS_CODE, "ERROR");
            log.error(x.getMessage());
            throw exception;
        } finally {
            MDC.clear();
        }
    }

    private void initMDC(String requestId, String partnerName, String serviceInstanceId, String remoteAddress) {
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

    /**
     * Given a service instance ID, GET the resources from Service Decompostion.
     */
    private ModelContext getServiceDeomposition(String serviceInstanceId, String partnerName, String requestId)
            throws DiscoveryException {
        if (serviceInstanceId == null) {
            return null;
        }

        log.info("Querying Service Decomposition for service instance " + serviceInstanceId);

        String urlStr = getUrl(serviceInstanceId);

        try {
            Response response = jerseyClient.target(urlStr).request()
                    .header("Accept", "application/json")
                    .header("Authorization", getSdBasicAuthorization())
                    .header("X-ONAP-PartnerName", partnerName)
                    .header("X-ONAP-RequestID", requestId).get();

            String reply = null;
            if (response.getStatus() != 200) {
                MDC.put(MDC_RESPONSE_CODE, String.valueOf(response.getStatus()));
                MDC.put(MDC_STATUS_CODE, "ERROR");
                throw new DiscoveryException(response.getStatusInfo().toString(),
                        Response.Status.fromStatusCode(response.getStatus()));
            } else {
                MDC.put(MDC_RESPONSE_CODE, String.valueOf(response.getStatus()));
                MDC.put(MDC_STATUS_CODE, "COMPLETE");
                reply = response.readEntity(String.class);

                log.info("GET Response from ServiceDecompositionMircoService GetContext for serviceInstanceId:"
                        + serviceInstanceId + ", message body: " + reply);
            }

            List<Object> jsonSpec = JsonUtils.filepathToList("config/networkdiscoveryspec.json");
            Object jsonInput = JsonUtils.jsonToObject(reply);
            Chainr chainr = Chainr.fromSpec(jsonSpec);
            Object transObject = chainr.transform(jsonInput);
            Gson gson = new Gson();
            return gson.fromJson(JsonUtils.toPrettyJsonString(transObject), ModelContext.class);
        } catch (Exception x) {
            throw new DiscoveryException(x.getMessage(), x);
        }
    }

    private String getUrl(String serviceInstanceId) throws DiscoveryException {
        String url = serviceDecompositionBaseUrl + "?serviceInstanceId=" + serviceInstanceId;
        return url;
    }

    private String getSdBasicAuthorization() {
        return serviceDecompositionBasicAuthorization;
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
            ;
        } else {
            throw new DiscoveryException(
                    "Missing Authorization: " + (authorization == null ? "null" : authorization.toString()),
                    Status.UNAUTHORIZED);
        }
    }

    @Override
    public void networkDiscoveryNotification(NetworkDiscoveryNotification ndNotification, String authorization)
            throws DiscoveryException {
        String requestId = ndNotification.getRequestId();
        initMDC_MsgFrom_networkDiscoveryMicroService(requestId);
        log.info("POST message payload:" + ndNotification.toString());
        String status = null;

        NetworkDiscoveryRspInfo myNetworkDiscoveryRspInfo;
        lock.lock();
        try {
            myNetworkDiscoveryRspInfo = networkDiscoveryInfoList.get(requestId);
            if (myNetworkDiscoveryRspInfo == null) {
                // The requestId is invalid. The corresponding request may
                // already be discarded
                // due to time out or error exception, or the request may never
                // exist.
                log.error("Unknown RequestId:" + requestId
                        + "! The corresponding request may already be discarded due to time out or error exception, or the request never exists.");

                status = MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_UNKNOWN_REQ;
                return;
            }

            // Update networkDiscoveryInfo
            status = MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_SUCCESS;
            myNetworkDiscoveryRspInfo.getNetworkDiscoveryNotificationList().add(ndNotification);
        } finally {
            lock.unlock();
        }

        MDC.put(MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS, status);
        CountDownLatch latch = myNetworkDiscoveryRspInfo.getLatchSignal();
        if (latch != null) {
            latch.countDown();
        }

        return;
    }

    private ModelContext updateServiceDecompCtx(ModelContext networkDiscoveryCtx,
            List<String> sentRequestIdList) {
        /*
         * TO DO: We can't add network discovery data to networkDiscoveryCtx
         * because the existing "v0" context aggregator context model doesn't
         * support it. We will have to wait for the real "v1" context model
         * which contains attributes, vservers and networks.
         */

        StringBuilder sbl = new StringBuilder();
        int idx = 0;
        for (String reqId : sentRequestIdList) {
            if (!(networkDiscoveryInfoList.containsKey(reqId))) {
                continue;
            }
            idx++;
            sbl.append("--[[Entry" + idx + "]]" + ", requestId:" + reqId);
            lock.lock();
            NetworkDiscoveryRspInfo tNdRspInfo = networkDiscoveryInfoList.get(reqId);

            // ServiceDecompCtx is updated, we need to delete the existing entry
            // in
            // networkDiscoveryInfoList
            networkDiscoveryInfoList.remove(reqId);
            lock.unlock();

            sbl.append(", Resource :" + tNdRspInfo.getResourceType());
            sbl.append(", ResourceId :" + tNdRspInfo.getResourceId());
            sbl.append(", RelatedRequestId :" + tNdRspInfo.getRelatedRequestIdList());
            for (NetworkDiscoveryNotification nt : tNdRspInfo.getNetworkDiscoveryNotificationList()) {
                List <Resource> resourceList = nt.getResources();
                for (Resource resource : resourceList) {
                    updateServiceDecompCtx(networkDiscoveryCtx, resource);
                }
                sbl.append(" Notification :" + nt.toString());
            }
        }

        String infoStr = sbl.toString();
        log.info(
                "updateServiceDecompCtx_and_networkDiscoveryInfoList: All Notifications from NetworkDiscoveryMicroService: "
                        + infoStr);

        return networkDiscoveryCtx;
    }

    private void updateServiceDecompCtx(ModelContext networkDiscoveryCtx, Resource resource) {
        for (VF vf : networkDiscoveryCtx.getVfs()) {
            for (VFModule vfModule : vf.getVfModules()) {
                for (VM vm : vfModule.getVms()) {
                    if (vm.getUuid().equals(resource.getId())) {
                        vm.setDataQuality(resource.getDataQuality());
                        if (null != resource.getAttributeList()) {
                            for (org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Attribute ndattribute : resource.getAttributeList()) {
                                try {
                                    Attribute attribute = new Attribute();
                                    attribute.setName(Attribute.Name.valueOf(ndattribute.getName()));
                                    attribute.setValue(ndattribute.getValue());
                                    attribute.setDataQuality(ndattribute.getDataQuality());
                                    vm.addAttribute(attribute);
                                } catch (IllegalArgumentException ex) {
                                    // The attribute Name passed back from Network Discovery is not in our enum
                                    log.info("Atribute Name: " + ndattribute.getName() + " for Resource:" + resource.getName() + " Id:"  +resource.getId() +  " is invalid");
                                }

                            }
                        }
                    }
                }
                for (Network network : vfModule.getNetworks()) {
                    if (network.getUuid().equals(resource.getId())) {
                        network.setDataQuality(resource.getDataQuality());
                        if (null != resource.getAttributeList()) {
                            for (org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Attribute ndattribute : resource.getAttributeList()) {
                                try {
                                    Attribute attribute = new Attribute();
                                    attribute.setName(Attribute.Name.valueOf(ndattribute.getName()));
                                    attribute.setValue(ndattribute.getValue());
                                    network.addAttribute(attribute);
                                } catch (IllegalArgumentException ex) {
                                    // The attribute Name passed back from Network Discovery is not in our enum
                                    log.info("Atribute Name: " + ndattribute.getName() + " for Resource:" + resource.getName() + " Id:"  + resource.getId() +  " is invalid");
                                }
                            }
                        }
                    }
                }

            }
        }
    }


    private CountDownLatch createCountDownLatch(ModelContext networkDiscoveryCtx) {

        // Obtain the possible total count of messages to NetworkDiscovery
        // for CountDownLatch.
        int latch_count = 0;
        for (VF vf : networkDiscoveryCtx.getVfs()) {
            latch_count += vf.getVnfcs().size();
            for (VFModule vfModule : vf.getVfModules()) {
                latch_count += vfModule.getVms().size();
                latch_count += vfModule.getNetworks().size();
            }
        }
        if (latch_count > 0) {
            // Let us create task that is going to
            // wait for all threads before it starts
            CountDownLatch latchSignal = new CountDownLatch(latch_count);
            return latchSignal;
        }

        return null;
    }

    /* Return list of requestIds sent to network-discovery microService. */
    private List<String> sendNetworkDiscoveryRequest(ModelContext networkDiscoveryCtx, String serviceInstanceId, String parent_requestId,
            String partner_name, CountDownLatch latchSignal) throws DiscoveryException {

        List<String> relatedRequestIdList = new ArrayList<String>();
        List<NdResource> ndresourceList = new ArrayList<NdResource>();

        for (VF vf : networkDiscoveryCtx.getVfs()) {
            for (VNFC vnfc : vf.getVnfcs()) {
                ndresourceList.add(new NdResource(vnfc.getNfcNamingCode(), vnfc.getUuid()));
            }
            for (VFModule vfModule : vf.getVfModules()) {
                for (VM vm : vfModule.getVms() ) {
                    ndresourceList.add(new NdResource(vm.getNfcNamingCode(), vm.getUuid()));
                }
                for (Network network : vfModule.getNetworks()) {
                    ndresourceList.add(new NdResource(network.getNfcNamingCode(), network.getUuid()));
                }
            }
        }
        for (NdResource resource : ndresourceList) {
            String origResourceType = resource.getResourceType();
            String resourceType = networkDiscoveryCtxBuilderResourceTypeMapping.get(origResourceType);
            if (resourceType == null) {
                log.error("Unable to find " + origResourceType + " from networkDiscoveryCtxBuilderResourceTypeMapping");
                continue;
            }

            // The old_requestId is inherited from ServiceDecomposition.
            // Before we send a
            // message to NetworkDiscoveryMicroService for each Resource, we
            // need to generate
            // a new request for identification, based on the old ID.
            String requestId = parent_requestId + NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER
                    + uniqueSeq.incrementAndGet();

            if (true == sendNetworkDiscoveryRequestToSpecificServer(partner_name, parent_requestId, requestId,
                    resource.getResourceId(), resourceType, latchSignal)) {
                relatedRequestIdList.add(requestId);
            }
        }

        // Update networkDiscoveryInfoList
        for (String t_rqId : relatedRequestIdList) {
            if (networkDiscoveryInfoList.containsKey(t_rqId)) {
                lock.lock();
                networkDiscoveryInfoList.get(t_rqId).setRelatedRequestIdList(relatedRequestIdList);
                lock.unlock();
            }
        }

        return relatedRequestIdList;
    }

    // Return true when message is sent to network-discovery microService,
    // otherwise, return false.
    private boolean sendNetworkDiscoveryRequestToSpecificServer(String partner_name, String parent_requestId,
            String requestId, String resourceId, String resourceType, CountDownLatch latchSignal)
            throws DiscoveryException {
        String callbackUrlStr = getNetworkDiscoveryCtxBuilderCallBackUrl();
        String networkDiscoveryUrl = networkDiscoveryMicroServiceBaseUrl;

        NetworkDiscoveryRspInfo entryNS = new NetworkDiscoveryRspInfo();
        entryNS.setRequestId(requestId);
        entryNS.setResourceId(resourceId);
        entryNS.setResourceType(resourceType);
        entryNS.setLatchSignal(latchSignal);
        List<NetworkDiscoveryNotification> notfList = new ArrayList<>();
        List<String> reqList = new ArrayList<>();
        entryNS.setNetworkDiscoveryNotificationList(notfList);
        entryNS.setRelatedRequestIdList(reqList);

        // Update networkDiscoveryInfoList before sending the message
        // to NetworkDiscoveryMicroService, in case of race condition.
        lock.lock();
        networkDiscoveryInfoList.put(requestId, entryNS);
        lock.unlock();

        // send message to Network Discovery API
        NetworkDiscoveryResponse ndResponse = null;

        // Prepare MDC for logs
        initMDC_sendTo_networkDiscoveryMicroService(networkDiscoveryUrl, requestId, resourceType, resourceId,
                callbackUrlStr, partner_name);

        try {
            Response response = jerseyClient.target(networkDiscoveryUrl)
                    .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID, requestId)
                    .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE, resourceType)
                    .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID, resourceId)
                    .queryParam(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_NOTIFICATION_URL, callbackUrlStr).request()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json").header(HttpHeaders.ACCEPT, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, getNetworkDiscoveryMircoServiceBasicAuthorization())
                    .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME, partner_name)
                    .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID, parent_requestId).get();

            int responseCode = response.getStatus();
            String status = Response.Status.fromStatusCode(response.getStatus()) + ",code:" + response.getStatus();
            if ((responseCode < 200) || (responseCode >= 300)) {
                latchSignal.countDown();
                safeRemoveEntry_from_networkDiscoveryInfoList(requestId);
                MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS, status);
                throw new DiscoveryException(response.getStatusInfo().toString(),
                        Response.Status.fromStatusCode(response.getStatus()));
            } else {
                ndResponse = response.readEntity(NetworkDiscoveryResponse.class);
                MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS, status);
            }
        } catch (Exception e) {
            latchSignal.countDown();
            safeRemoveEntry_from_networkDiscoveryInfoList(requestId);
            MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS, e.getMessage());
            throw new DiscoveryException(e.getMessage(), e);
        }

        if (true == ndResponse.getAckFinalIndicator()) {
            // Perform count-down because there is no more notification coming
            // for this requestId.
            latchSignal.countDown();
            safeRemoveEntry_from_networkDiscoveryInfoList(requestId);
            MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS,
                    MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_STATUS_NO_MORE_CALL_BACK);
        }

        log.info("Message sent. Response Payload:" + ndResponse);
        return true;
    }

    private void safeRemoveEntry_from_networkDiscoveryInfoList(String requestId) {
        lock.lock();
        networkDiscoveryInfoList.remove(requestId);
        lock.unlock();
    }

    private String getNetworkDiscoveryCtxBuilderCallBackUrl() {
        String url = networkDiscoveryCtxBuilderBaseUrl
                + NETWORK_DISCOVERY_CTX_BUILDER_NETWORK_DISCOVERY_NOTIFICATION_PATH;
        return url;
    }

    private String getNetworkDiscoveryMircoServiceBasicAuthorization() {
        return networkDiscoveryMicroServiceBasicAuthorization;
    }

    private void initMDC_sendTo_networkDiscoveryMicroService(String networkDiscoveryUrl, String requestId,
            String resourceType, String resourceId, String callbackUrlStr, String partner_name) {
        String parentRequestId = MDC.get(MDC_REQUEST_ID);
        String parentServiceInstanceId = MDC.get(MDC_SERVICE_INSTANCE_ID);
        String parentPartnerName = MDC.get(MDC_PARTNER_NAME);

        MDC.clear();
        initMDC(parentRequestId, parentPartnerName, parentServiceInstanceId, networkDiscoveryMicroServiceHostAndPort);

        MDC.put(MDC_SERVICE_NAME, MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_APP);
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_MSG_NAME,
                MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_FINDBYRESOURCEIDANDTYPE);
        MDC.put(MDC_START_TIME, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_URL, networkDiscoveryUrl);
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_REQUEST_ID, requestId);
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_RESOURCE_TYPE, resourceType);
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_RESOURCE_ID, resourceId);
        MDC.put(MDC_TO_NETWORK_DISCOVERY_MICRO_SERVICE_CALL_BACK_URL, callbackUrlStr);
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            // If, for some reason we are unable to get the canonical host name,
            // we
            // just want to leave the field null.
            log.info("Could not get canonical host name for " + MDC_SERVER_FQDN + ", leaving field null");
        }
    }

    private void initMDC_MsgFrom_networkDiscoveryMicroService(String requestId) {
        String parentRequestId = MDC.get(MDC_REQUEST_ID);
        String parentServiceInstanceId = MDC.get(MDC_SERVICE_INSTANCE_ID);
        String parentPartnerName = MDC.get(MDC_PARTNER_NAME);

        MDC.clear();
        initMDC(parentRequestId, parentPartnerName, parentServiceInstanceId, networkDiscoveryMicroServiceHostAndPort);

        MDC.put(MDC_SERVICE_NAME, MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_APP);
        MDC.put(MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_MSG_NAME,
                MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_NETWORKDISCOVERYNOTIFICATION);
        MDC.put(MDC_START_TIME, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));
        MDC.put(MDC_FROM_NETWORK_DISCOVERY_MICRO_SERVICE_REQUEST_ID, requestId);
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            // If, for some reason we are unable to get the canonical host name,
            // we
            // just want to leave the field null.
            log.info("Could not get canonical host name for " + MDC_SERVER_FQDN + ", leaving field null");
        }
    }

    protected static void updateNetworkDiscoveryInfoList(String requestId, NetworkDiscoveryRspInfo resp) {
        lock.lock();
        networkDiscoveryInfoList.put(requestId, resp);
        lock.unlock();
        return;
    }

    protected static NetworkDiscoveryRspInfo getNetworkDiscoveryInfoList(String requestId) {
        lock.lock();
        NetworkDiscoveryRspInfo returnInfo = networkDiscoveryInfoList.get(requestId);
        lock.unlock();
        return returnInfo;
    }

}
