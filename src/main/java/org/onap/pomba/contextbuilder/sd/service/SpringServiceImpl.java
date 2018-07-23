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
package org.onap.pomba.contextbuilder.sd.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.pomba.auditcommon.datatypes.ModelContext;
import org.onap.pomba.auditcommon.datatypes.Service;
import org.onap.pomba.auditcommon.datatypes.VF;
import org.onap.pomba.auditcommon.datatypes.VFModule;
import org.onap.pomba.auditcommon.datatypes.VNFC;
import org.onap.pomba.contextbuilder.sd.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.sd.model.HelloWorld;
import org.onap.pomba.contextbuilder.sd.service.rs.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;


@org.springframework.stereotype.Service
public class SpringServiceImpl implements SpringService {
    private static Logger log = LoggerFactory.getLogger(RestService.class);
    public static String APP_NAME = "NetworkDiscoveryContextBuilder";

    public static String MDC_REQUEST_ID = "RequestId";
    public static String MDC_SERVER_FQDN = "ServerFQDN";
    public static String MDC_SERVICE_NAME = "ServiceName";
    public static String MDC_PARTNER_NAME = "PartnerName";
    public static String MDC_START_TIME = "StartTime";
    public static String MDC_REMOTE_HOST = "RemoteHost";
    public static String MDC_SERVICE_INSTANCE_ID = "ServiceInstanceId";
    public static String MDC_CLIENT_ADDRESS = "ClientAddress";

    private static final String ENTITY_GENERIC_VNFS = "generic-vnfs";
    private static final String ENTITY_L3_NETWORK = "l3-network";
    private static final String ENTITY_MODEL_INVARIANT_ID = "model-invariant-id";
    private static final String ENTITY_NETWORK_ID = "network-id";
    private static final String ENTITY_NETWORK_NAME = "network-name";
    private static final String ENTITY_SERVICE_INSTANCE_ID = "service-instance-id";
    private static final String ENTITY_SERVICE_INSTANCE_NAME = "service-instance-name";
    private static final String ENTITY_VF_MODULE = "vf-module";
    private static final String ENTITY_VF_MODULES = "vf-modules";
    private static final String ENTITY_VF_MODULE_ID = "vf-module-id";
    private static final String ENTITY_VNF_ID = "vnf-id";
    private static final String ENTITY_VNF_NAME = "vnf-name";
    private static final String ENTITY_VNF_TYPE = "vnf-type";
    private static final String ENTITY_VSERVER = "vserver";
    private static final String ENTITY_VSERVER_NAME = "vserver-name";
    private static final String ENTITY_VSERVER_ID = "vserver-id";

    @Autowired
    private String sdBaseUrl;

    @Autowired
    private String ndBasicAuthorization;

    @Autowired
    private String sdBasicAuthorization;

    @Override
    public HelloWorld getQuickHello(String name) {
        if (name == null || name.isEmpty()) {
            name = "world";
        }
        String message = "Hello " + name + "!";
        HelloWorld hello = new HelloWorld(message);
        return hello;
    }

    @Override
    public ModelContext getContext(String partnerName,
                              String authorization,
                              String requestId,
                              String serviceInstanceId,
                              String modelVersionId,
                              String modelInvariantId) throws DiscoveryException {


        initMDC(requestId, partnerName, null);
        try {
            log.info("Querying A&AI for service instance " + serviceInstanceId);
            //ServiceInstance serviceInstance = AAICannedData.getServiceInstance(serviceInstanceId);
            return getServiceDeomposition(serviceInstanceId, partnerName, authorization, requestId);
        } finally {
            MDC.clear();
        }
    }


    private void initMDC(String transId, String partnerName, String remoteAddress) {
        // TODO Auto-generated method stub
        MDC.clear();
        MDC.put(MDC_REQUEST_ID, transId);
        MDC.put(MDC_SERVICE_NAME, APP_NAME);
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        MDC.put(MDC_PARTNER_NAME, partnerName);
        MDC.put(MDC_CLIENT_ADDRESS, remoteAddress);
        MDC.put(MDC_START_TIME, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));

        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName());
        } catch (Exception e) {
            // If, for some reason we are unable to get the canonical host name, we
            // just want to leave the field unpopulated. There is not much value
            // in doing anything else with an exception at this point.
        }
    }

    /**
     * Given a service instance ID, GET the resources from Service Decompostion.
     */
    private ModelContext getServiceDeomposition(String serviceInstanceId, String partnerName, String authorization, String requestId) throws DiscoveryException {
        if (serviceInstanceId == null) {
            return null;
        }

        String urlStr = getUrl(serviceInstanceId);

        try {
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", getSdBasicAuthorization());
            conn.setRequestProperty("X-ONAP-PartnerName", partnerName);
            conn.setRequestProperty("X-ONAP-RequestID", requestId);

            BufferedReader br = null;
            JSONObject jObject = null;
            if (conn.getResponseCode() != 200) {
                throw new DiscoveryException(conn.getResponseMessage(), Response.Status.fromStatusCode(conn.getResponseCode()));
            }
            else {
                br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            }

            if (null != br) {
                StringBuilder sb = new StringBuilder();
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                }
                log.info("Response from GET " + url.toURI() + " " + sb.toString());
                jObject = new JSONObject(sb.toString());
                br.close();
            }

            return parseServiceDecomposition(jObject);
        }  catch (Exception x) {
            log.error("getServiceDeomposition failed", x);
            throw new DiscoveryException(x.getMessage(), x);
        }
    }

    private String getUrl (String serviceInstanceId) throws DiscoveryException {
        return new String(sdBaseUrl + "?serviceInstanceId=" + serviceInstanceId);
    }

    private String getSdBasicAuthorization() {
        return sdBasicAuthorization;
    }

    private ModelContext parseServiceDecomposition(JSONObject jObject) {

        ModelContext response = new ModelContext();
        // Get Service Instance Data
        Service service = new Service();

        if (jObject.has(ENTITY_SERVICE_INSTANCE_NAME)) {
            service.setName(jObject.getString(ENTITY_SERVICE_INSTANCE_NAME));
        }

        if (jObject.has(ENTITY_SERVICE_INSTANCE_ID)) {
            service.setUuid(jObject.getString(ENTITY_SERVICE_INSTANCE_ID));
        }

        if (jObject.has(ENTITY_MODEL_INVARIANT_ID)) {
            service.setInvariantUuid(jObject.getString(ENTITY_MODEL_INVARIANT_ID));
        }

        response.setService(service);

        // Find generic-vnfs
        if (jObject.has(ENTITY_GENERIC_VNFS)) {
            JSONArray genericVnfs = jObject.getJSONArray(ENTITY_GENERIC_VNFS);
            for (int i = 0; i < genericVnfs.length(); i++ ) {
                VF vf = new VF();
                JSONObject genericVnfInst = genericVnfs.getJSONObject(i);

                if (genericVnfInst.has(ENTITY_VNF_NAME)) {
                    vf.setName(genericVnfInst.getString(ENTITY_VNF_NAME));
                }
                if (genericVnfInst.has(ENTITY_VNF_TYPE)) {
                    vf.setType(genericVnfInst.getString(ENTITY_VNF_TYPE));
                }
                if (genericVnfInst.has(ENTITY_VNF_ID)) {
                    vf.setUuid(genericVnfInst.getString(ENTITY_VNF_ID));
                }

                if (genericVnfInst.has(ENTITY_MODEL_INVARIANT_ID)) {
                    vf.setInvariantUuid(genericVnfInst.getString(ENTITY_MODEL_INVARIANT_ID));
                }

                // find vf-modules
                if (genericVnfInst.has(ENTITY_VF_MODULES)) {
                    JSONObject vfModules = genericVnfInst.getJSONObject(ENTITY_VF_MODULES);
                    if (vfModules.has(ENTITY_VF_MODULE)) {
                        JSONArray vfModuleList = vfModules.getJSONArray(ENTITY_VF_MODULE);
                        for (int j = 0; j < vfModuleList.length(); j++) {
                            VFModule vfModule = new VFModule();
                            JSONObject vfModuleInst = vfModuleList.getJSONObject(j);
                            if (vfModuleInst.has(ENTITY_VF_MODULE_ID)) {
                                vfModule.setUuid(vfModuleInst.getString(ENTITY_VF_MODULE_ID));
                            }
                            if (vfModuleInst.has(ENTITY_MODEL_INVARIANT_ID)) {
                                vfModule.setInvariantUuid(vfModuleInst.getString(ENTITY_MODEL_INVARIANT_ID));
                            }
                            vf.addVfModule(vfModule);
                        }
                    }
                }

                // Find vservers
                if (genericVnfInst.has(ENTITY_VSERVER)) {
                    JSONArray vservers = genericVnfInst.getJSONArray(ENTITY_VSERVER);
                    for (int j = 0; j < vservers.length(); j++) {
                        VNFC vserver  = new VNFC();
                        JSONObject vserversInst = vservers.getJSONObject(j);
                        if (vserversInst.has(ENTITY_VSERVER_NAME)) {
                            vserver.setName(vserversInst.getString(ENTITY_VSERVER_NAME));
                        }
                        if (vserversInst.has(ENTITY_VSERVER_ID)) {
                            vserver.setUuid(vserversInst.getString(ENTITY_VSERVER_ID));
                        }
                        if (vserversInst.has(ENTITY_MODEL_INVARIANT_ID)) {
                            vserver.setInvariantUuid(vserversInst.getString(ENTITY_MODEL_INVARIANT_ID));
                        }
                        vf.addVnfc(vserver);
                    }
                }

                // Find l3 networks
                if (genericVnfInst.has(ENTITY_L3_NETWORK)) {
                    JSONArray l3Networks = genericVnfInst.getJSONArray(ENTITY_L3_NETWORK);
                    for (int j = 0; j < l3Networks.length(); j++) {
                        VNFC l3Network = new VNFC();
                        JSONObject l3NetworkInst = l3Networks.getJSONObject(j);
                        if (l3NetworkInst.has(ENTITY_NETWORK_NAME)) {
                            l3Network.setName(l3NetworkInst.getString(ENTITY_NETWORK_NAME));
                        }
                        if (l3NetworkInst.has(ENTITY_NETWORK_ID)) {
                            l3Network.setUuid(l3NetworkInst.getString(ENTITY_NETWORK_ID));
                        }
                        if (l3NetworkInst.has(ENTITY_MODEL_INVARIANT_ID)) {
                            l3Network.setInvariantUuid(l3NetworkInst.getString(ENTITY_MODEL_INVARIANT_ID));
                        }
                        vf.addVnfc(l3Network);
                    }
                }
                response.addVf(vf);
            }
        }

        return response;
    }

    /**
     * Validates the Basic authorization header as admin:admin.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public void validateBasicAuth(String authorization) throws DiscoveryException {
        if (authorization != null && !authorization.trim().isEmpty() && authorization.startsWith("Basic")) {
            if (!authorization.equals(ndBasicAuthorization)) {
                throw new DiscoveryException("Authorization Failed", Status.UNAUTHORIZED);
            };
        } else {
            throw new DiscoveryException("Missing Authorization: " +(authorization==null ? "null" : authorization.toString()), Status.UNAUTHORIZED);
        }
    }


}