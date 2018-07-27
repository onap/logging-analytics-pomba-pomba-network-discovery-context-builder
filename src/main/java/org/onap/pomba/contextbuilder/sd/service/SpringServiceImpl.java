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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.pomba.auditcommon.datatypes.ModelContext;
import org.onap.pomba.auditcommon.datatypes.Service;
import org.onap.pomba.auditcommon.datatypes.VF;
import org.onap.pomba.auditcommon.datatypes.VFModule;
import org.onap.pomba.auditcommon.datatypes.VNFC;
import org.onap.pomba.contextbuilder.sd.JerseyConfiguration;
import org.onap.pomba.contextbuilder.sd.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.sd.model.HelloWorld;
import org.onap.pomba.contextbuilder.sd.model.NetworkDiscoveryRspInfo;
import org.onap.pomba.contextbuilder.sd.service.rs.RestService;
//import org.onap.pomba.contextbuilder.sd.util.SignalBarrier;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    public static String MDC_NUM_MSG_TO_NETWORK_DISCOVERY = "NumOfMsg_To_NetworkDiscovery";    
    public static String MDC_CLIENT_ADDRESS = "ClientAddress";
     
    public static String NETWORK_DISCOVERY_RSP_STATE_REQUEST_SENT = "RequestSent";
    public static String NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED = "AllRspReceived";
    public static String NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER = "___";
    public static String NETWORK_DISCOVERY_CTX_BUILDER_NETWORK_DISCOVERY_NOTIFICATION_PATH = "/network-discovery/service/networkDiscoveryNotification";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_GET = "GET";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_CONTENT_TYPE = "Content-Type";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_ACCEPT = "Accept";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_AUTHORIZATION = "Authorization";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME = "X-ONAP-PartnerName";    		  
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID = "X-ONAP-RequestID";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID = "requestId";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE = "resourceType";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID = "resourceId";
    public static String NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_NOTIFICATION_URL = "notificationURL";
    
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
    private static final String RESOURCE_TYPE_VSERVER = "vserver";
    
    private static Map <String, NetworkDiscoveryRspInfo> networkDiscoveryInfoList=  new HashMap<>();
    
    @Autowired
    private String sdBaseUrl;

    @Autowired
    private String ndBasicAuthorization;

    @Autowired
    private String sdBasicAuthorization;

    @Autowired
    private String networkDiscoveryBaseUrl;

    @Autowired
    private String networkDiscoveryCtxBuilderBaseUrl;

    @Autowired
    private long ndResponseTimeOutInMilliseconds;

    public SpringServiceImpl() {
    }
    
    private final ReentrantLock lock = new ReentrantLock();
    
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
            ModelContext serviceDecompCtx = getServiceDeomposition(serviceInstanceId, partnerName, authorization, requestId);
            List <String> sentRequestIdList = new ArrayList<>();
            CountDownLatch latchSignal = sendNetworkDiscoveryRequest(serviceDecompCtx, serviceInstanceId, partnerName, authorization ,sentRequestIdList); 
 
            int numOfMsgSent = sentRequestIdList.size();
            if  (( numOfMsgSent > 0) && ( latchSignal != null) ) {
        	 MDC.put(MDC_NUM_MSG_TO_NETWORK_DISCOVERY, Integer.toString(numOfMsgSent));
        	 
	   	     try {
	            // The main task waits for four threads
	            if (false == latchSignal.await(ndResponseTimeOutInMilliseconds,TimeUnit.MILLISECONDS )) {
	    	       // When it comes here, it is due to time out.
	            	log.info("Wait for Latch Signal time out " + serviceInstanceId);			            	          	
	            }	
	            
                return updateServiceDecompCtx_and_networkDiscoveryInfoList (serviceDecompCtx, sentRequestIdList);

	 	     } catch (InterruptedException ex) {
	 	        // handle interruption ...
	 	     } 		        	
                                  	
            } else {
            	return serviceDecompCtx;
            }
            } finally {
            MDC.clear();
        }
        
        return null;
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
        MDC.put(MDC_NUM_MSG_TO_NETWORK_DISCOVERY, "");
        
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

  	@Override	
  public void networkDiscoveryNotification(NetworkDiscoveryNotification ndNotification, String authorization) throws DiscoveryException {				
  		String requestId = ndNotification.getRequestId();
  		NetworkDiscoveryRspInfo myNetworkDiscoveryRspInfo = networkDiscoveryInfoList.get(requestId);

  		if (myNetworkDiscoveryRspInfo == null) {
  			// The requestId is invalid.
  			return;
  		}
  		
        // Update networkDiscoveryInfo  
	    List<NetworkDiscoveryNotification> notificationList = myNetworkDiscoveryRspInfo.getNetworkDiscoveryNotificationList();
		if ((notificationList == null) || (notificationList.isEmpty()) ){
           myNetworkDiscoveryRspInfo.setNetworkDiscoveryNotificationList(Arrays.asList(ndNotification));       	  	    
		}
		else {  					
			notificationList.add(ndNotification);
			myNetworkDiscoveryRspInfo.setNetworkDiscoveryNotificationList(notificationList); 
		}

		CountDownLatch latch = myNetworkDiscoveryRspInfo.getLatchSignal();
		if ( latch != null) {
			latch.countDown();
		}
		
		myNetworkDiscoveryRspInfo.setStatus(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED);
		
        lock.lock();
        networkDiscoveryInfoList.put(requestId, myNetworkDiscoveryRspInfo); 
	    lock.unlock();
  		  	    
        return;
  	}
  	
    private ModelContext updateServiceDecompCtx_and_networkDiscoveryInfoList (ModelContext serviceDecompCtx, List <String> sentRequestIdList ) {
  	 /* TO DO: We can’t add network discovery data to serviceDecompCtx because the existing “v0” context aggregator 
  	    context model doesn’t support it.  We will have to wait for the real “v1” context 
        model which contains attributes, vservers and networks. */
      
    	StringBuilder sbl = new StringBuilder();
    	for (String reqId : sentRequestIdList) {
    		sbl.append("\n requestId:"+reqId);
    		NetworkDiscoveryRspInfo tNdRspInfo = networkDiscoveryInfoList.get(reqId);
    		List<NetworkDiscoveryNotification> nList = tNdRspInfo.getNetworkDiscoveryNotificationList();
    		if (nList != null) {
        		for ( NetworkDiscoveryNotification nt : tNdRspInfo.getNetworkDiscoveryNotificationList()) {
        			sbl.append("\n -- Notification :"+ nt.toString() );
        		}    			
    		}
    	}

    	String infoStr = sbl.toString();
    	log.info("Received Notification from NetworkDiscoveryMicroService: " + infoStr);
    	 
      // ServiceDecompCtx is updated, we need to delete the existing entry in networkDiscoveryInfoList
      if (sentRequestIdList != null) {
          for ( String thisRequstId : sentRequestIdList) {    	  
      	  	  lock.lock();
        	  networkDiscoveryInfoList.remove(thisRequstId);
      	  	  lock.unlock();
          }      	  
      }

      return serviceDecompCtx;
    }
    
    // Create countDownLatch for each message sending to NetworkDiscovery
    private void startCountDownLatch (int size, CountDownLatch  latch) {        
    	for (int idx = 0; idx < size ; idx ++) {
    		String name = "countDownLatch" + idx;
    		int delay = 0; 
    		networkDiscoveryNotifCountDownLatchElement tLatch = new networkDiscoveryNotifCountDownLatchElement (delay,latch, name );
    		tLatch.start();
    	}
    	return;
    }
    
    /* Return list of requestIds sent to network-discovery microService. */
    private CountDownLatch  sendNetworkDiscoveryRequest (ModelContext serviceDecompCtx, String serviceInstanceId , String partner_name, String authorization,
    		List <String> relatedRequestIdList)  throws DiscoveryException{
	    log.info("Http Request", "GET");
	     String parent_requestId = MDC.get(MDC_REQUEST_ID);
	     
	     // Obtain the possible total count of messages to NetworkDiscovery
	     // for CountDownLatch.
	     int latch_count = sizeOfMsgToNetworkDiscovery (serviceDecompCtx);
	     if (latch_count > 0 ) {
	         // Let us create task that is going to 
	         // wait for all threads before it starts
	    	 CountDownLatch latchSignal = new CountDownLatch(latch_count);
	         startCountDownLatch (latch_count , latchSignal );
	         
		     List <VF> vfList = serviceDecompCtx.getVf();		     
		    		 
		     for (VF entryVF: vfList){
		    	 
		     	List <VNFC> vnfcList = entryVF.getVnfc();
		     	for (VNFC entryVnfc : vnfcList){
		       		String vserverId = entryVnfc.getUuid();  
		       		Random rand = new Random(); 
		       		int randValue = rand.nextInt(2000); 
		       		// The old_requestId is inheritated from ServiceDecomposition. Before we send a 
		       		// message to NetworkDiscoveryMicroService for each Vserver, we need to generate 
		       		// a new request for identification, based on the old ID.
		       		
		       		String requestId =  parent_requestId + NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER + randValue;
		
		     		if ( true == sendNetworkDiscoveryRequestToSpecificVserver(partner_name, authorization,parent_requestId, requestId, vserverId, RESOURCE_TYPE_VSERVER, latchSignal)) {
		     			relatedRequestIdList.add(requestId);
		     		}
		       	}    	 
		     }
		     
		     // Update networkDiscoveryInfoList
		     for (String t_rqId : relatedRequestIdList) {
		    	 networkDiscoveryInfoList.get(t_rqId).setRelatedRequestIdList(relatedRequestIdList);
		     }
		     	        	    
		     return latchSignal;		         
	     }

	     return null;
    }	

    /* Return number of messages sent to network-discovery microService. */
    private int  sizeOfMsgToNetworkDiscovery (ModelContext serviceDecompCtx){
	     int msg_count = 0;
	     
	     List <VF> vfList = serviceDecompCtx.getVf();	    		 
	     for (VF entryVF: vfList){
	     	List <VNFC> vnfcList = entryVF.getVnfc();
	     	msg_count = msg_count + vnfcList.size();  	 
	     }	     
	     return msg_count;
    }	
   
    // Return true when message is sent to network-discovery microService, otherwise, return false.
    private boolean sendNetworkDiscoveryRequestToSpecificVserver (String partner_name, String authorization, String parent_requestId, String requestId,String vserverId,String resourceType,
    		  CountDownLatch latchSignal) throws DiscoveryException{
  	    String callbackUrlStr = getNetworkDiscoveryCtxBuilderCallBackUrl();
  	    String networkDiscoveryUrl = getNetworkDiscoveryUrl(resourceType, requestId, callbackUrlStr, vserverId );
  	    
      	NetworkDiscoveryRspInfo entryNS = new NetworkDiscoveryRspInfo();
 		entryNS.setRequestId(requestId);
 		entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_REQUEST_SENT);
 		entryNS.setvServerId(vserverId);

 		// send message to Network Discovery API    		     		
        NetworkDiscoveryResponse ndResponse = null;
          
  		try {
            JerseyConfiguration jerseyConfiguration = new JerseyConfiguration();
            Client client = jerseyConfiguration.jerseyClient();

            Response response =
                    client.target(networkDiscoveryUrl)
                        .request()
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_CONTENT_TYPE, "application/json")
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_ACCEPT, "application/json")
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_AUTHORIZATION, authorization)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME, partner_name)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID, parent_requestId)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID, requestId)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE, resourceType)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID, vserverId)
                        .header(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_NOTIFICATION_URL, callbackUrlStr)
                        .get(); 
            
            int responseCode = response.getStatus(); 
            if ((responseCode < 200) || (responseCode >= 300)) {
                latchSignal.countDown();
                throw new DiscoveryException(response.getStatusInfo().toString(), Response.Status.fromStatusCode(response.getStatus()));
            }
            else {
            	ndResponse = response.readEntity(NetworkDiscoveryResponse.class);
                log.info("Response from GET " + networkDiscoveryUrl + " " + ndResponse.toString());                
            }
                        
  		} catch (Exception e) {
            latchSignal.countDown();
            throw new DiscoveryException(e.getMessage(), e);
  		}
     		
    	        
     	// Update networkDiscoveryInfo   	       
        entryNS.setLatchSignal(latchSignal);
        
        if ( true == ndResponse.getAckFinalIndicator()) {
        	// Perform count-down because there is no more notification coming for this requestId.
            latchSignal.countDown();

        	// This indicates no more call-back from Network Discovery MicroService.
        	entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED);
        }
        
        lock.lock();
        networkDiscoveryInfoList.put(requestId, entryNS);
	    lock.unlock();
	    
        return true;
          
    }

	private String getNetworkDiscoveryCtxBuilderCallBackUrl () {
		return new String(networkDiscoveryCtxBuilderBaseUrl
				+ NETWORK_DISCOVERY_CTX_BUILDER_NETWORK_DISCOVERY_NOTIFICATION_PATH
             );
	}
      
	private String getNetworkDiscoveryUrl (String resourceType, String requestId, String notificationURL, String resourceId) throws DiscoveryException {
        String findReasourceByIdAndType_path = networkDiscoveryBaseUrl
				+ "?resourceType=" + resourceType
				+ "&requestId=" + requestId
				+ "&notificationURL=" + notificationURL
				+ "&resourceId=" + resourceId;
        
        return findReasourceByIdAndType_path;
	}
	
    // A class to represent threads for which
    // the main thread waits.
  	private class networkDiscoveryNotifCountDownLatchElement extends Thread 
  	{
  	    private int delay;
  	    private CountDownLatch latch;
  	 
  	    public networkDiscoveryNotifCountDownLatchElement(int delay, CountDownLatch latch,
  	                                    String name)
  	    {
  	        super(name);
  	        this.delay = delay;
  	        this.latch = latch;
  	    }
  	 
  	    @Override
  	    public void run() 
  	    {
  	        try
  	        {
  	            Thread.sleep(delay);
  	        }
  	        catch (InterruptedException e)
  	        {
  	        	log.info("Exception "+ e.getMessage());
  	        }
  	    }
  	}
}