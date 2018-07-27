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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.tomcat.jni.Lock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.pomba.auditcommon.datatypes.ModelContext;
import org.onap.pomba.auditcommon.datatypes.Service;
import org.onap.pomba.auditcommon.datatypes.VF;
import org.onap.pomba.auditcommon.datatypes.VFModule;
import org.onap.pomba.auditcommon.datatypes.VNFC;
import org.onap.pomba.contextbuilder.sd.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.sd.model.HelloWorld;
import org.onap.pomba.contextbuilder.sd.model.NetworkDiscoveryRspInfo;
import org.onap.pomba.contextbuilder.sd.service.rs.RestService;
import org.onap.pomba.contextbuilder.sd.util.SignalBarrier;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
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
    private static List<NetworkDiscoveryRspInfo> networkDiscoveryInfoList= new ArrayList<>();
    
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
    private long ndResponseTimeOutInNanoseconds;

    
    private SignalBarrier barrier = null;

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
            if (true == sendNetworkDiscoveryRequest(serviceDecompCtx, serviceInstanceId, partnerName, authorization )){
                // Set up Signal Barrier (Semaphore) to wait for multiple incoming messages.
            	// Either all responses were received or time-out, we will continue to the process.
            	// and then update ServiceDecompCtx
                if (barrier == null) {
                  barrier = new SignalBarrier();
                }
                
                try {
                	// Convert to nanosecond (1 second = 1000 milliseconds = 1,000,000,000 nanoseconds)
                    long remainingTimeOut = ndResponseTimeOutInNanoseconds;
                    while ((!isReadyToResponseToServiceDecomposition(requestId)) && (remainingTimeOut > 0)) {
                    	remainingTimeOut = barrier.awaitNanos(remainingTimeOut);                    
                    }
                    
                    barrier = null;
                    return updateServiceDecompCtx_and_networkDiscoveryInfoList (serviceDecompCtx, requestId);
                    
                } catch (Exception e) {
        			// TODO Auto-generated catch block
                    log.error("Signal Barrier failed", e);
                    throw new DiscoveryException(e.getMessage(), e);			
                }                                  	
            } else {
            	return serviceDecompCtx;
            }
            } finally {
            MDC.clear();
        }
    }

    private ModelContext updateServiceDecompCtx_and_networkDiscoveryInfoList (ModelContext serviceDecompCtx, String  parent_requestId ) {
  	 /* TO DO: We can’t add network discovery data to serviceDecompCtx because the existing “v0” context aggregator 
  	    context model doesn’t support it.  We will have to wait for the real “v1” context 
        model which contains attributes, vservers and networks. */
    	
      // ServiceDecompCtx is updated, we need to delete the existing networkDiscoveryInfoList
      for ( NetworkDiscoveryRspInfo entry : networkDiscoveryInfoList) {
    	  if (entry.getParentRequestId().equals(parent_requestId)) {
    	  	  	  lock.lock();
    	  	  	  networkDiscoveryInfoList.remove(entry); 
    	  	  	  lock.unlock();
   	  	}
      }  
 
      return serviceDecompCtx;
    }
    
    private boolean isReadyToResponseToServiceDecomposition (String parent_requestId) {
  	  for ( NetworkDiscoveryRspInfo entry : networkDiscoveryInfoList) {
  	  	if ((entry.getParentRequestId().equals(parent_requestId)) && 
  	  	(!(entry.getStatus().equals(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED)) ) ){
  	  		return false;
  	  	}       	  	
  	  }  
  	  
  	  // When it comes here, all responses were received, then we will exit the wait in the calling method.
  	  if (barrier != null) {
  	  	  barrier.signal();  		  
  	  }
  	  return true;
    }
    
    /* Return true when message is sent to network-discovery microService, otherwise, return false. */
    private boolean sendNetworkDiscoveryRequest (ModelContext serviceDecompCtx, String serviceInstanceId , String partner_name, String authorization)  throws DiscoveryException{
    log.info("Http Request", "GET");
     boolean messageSent = false;  
     String parent_requestId = MDC.get(MDC_REQUEST_ID);
            
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

     		if ( true == sendNetworkDiscoveryRequestToSpecificVserver(partner_name, authorization,parent_requestId, requestId, vserverId, RESOURCE_TYPE_VSERVER)) {
     			messageSent = true;
     		}
       	}    	 
         }
     return messageSent;
    }	

    
      // Return true when message is sent to network-discovery microService, otherwise, return false.
      private boolean sendNetworkDiscoveryRequestToSpecificVserver (String partner_name, String authorization, String parent_requestId, String requestId,String vserverId,String resourceType ) throws DiscoveryException{
  	    boolean requestSent = false;
  	    String callbackUrlStr = getNetworkDiscoveryCtxBuilderCallBackUrl();
  	    String networkDiscoveryUrl = getNetworkDiscoveryUrl(resourceType, requestId, callbackUrlStr, vserverId );
  	    
      	NetworkDiscoveryRspInfo entryNS = new NetworkDiscoveryRspInfo();
     		entryNS.setRequestId(requestId);
     		entryNS.setParentRequestId(parent_requestId);     		
     		entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_REQUEST_SENT);
     		entryNS.setvServerId(vserverId);

     		// send message to Network Discovery API
     		
          HttpURLConnection conn = null;
  		try {
  			  URL url = new URL( networkDiscoveryUrl );
  			  conn = (HttpURLConnection) url.openConnection();
  			  conn.setRequestMethod(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_GET);
  			  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_CONTENT_TYPE, "application/json");
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_ACCEPT, "application/json");
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_AUTHORIZATION, authorization);
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_PARTNER_NAME, partner_name);    		  
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_X_ONAP_REQUEST_ID, parent_requestId);
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_REQUEST_ID, requestId);
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_TYPE, resourceType);
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_RESOURCE_ID, vserverId);
      		  conn.setRequestProperty(NETWORK_DISCOVERY_FIND_RESOURCE_BY_TYPE_REST_NOTIFICATION_URL, callbackUrlStr);

  		} catch (IOException e) {
            log.error("HttpURLConnection failed", e);
            throw new DiscoveryException(e.getMessage(), e);			
  		}
     		
  		BufferedReader br = null;    		
        JSONObject jObject = null;   		  
        NetworkDiscoveryResponse  ndResponse = null; 
          try {
        	int responseCode = conn.getResponseCode();  
  			if ((responseCode < 200) || (responseCode >= 300)) {
  				throw new DiscoveryException(conn.getResponseMessage(), Response.Status.fromStatusCode(conn.getResponseCode()));
  			}
  			else {
  				
        	    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));				
                if (null != br) {
                    StringBuilder sb = new StringBuilder();
                    String readLine;
    
          	        while ((readLine = br.readLine()) != null) {
          		       sb.append(readLine);
                 	}
          	        
          	        log.info("Response from GET " + sb.toString());
          	        jObject = new JSONObject(sb.toString());
          	        br.close();
                }   		  
		  
		        // Retrieve payload and store to networkDiscoveryInfoList
         	    Gson gson= new Gson();
    	        ndResponse= gson.fromJson(jObject.toString(),NetworkDiscoveryResponse.class);
    	        
             	// Update networkDiscoveryInfo   	       
  	            entryNS.setNetworkDiscoveryRspList(Arrays.asList(ndResponse));       	  	    

    	        if ( true == ndResponse.getAckFinalIndicator()) {
    	        	entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED);
    	        }
    	        
    	        lock.lock();
      		    networkDiscoveryInfoList.add(entryNS);  
      		    lock.unlock();
      		    
  				requestSent = true;    	        

  			}
  		} catch (IOException e) {
            log.error("HttpURLConnection failed", e);
            throw new DiscoveryException(e.getMessage(), e);	
        }
          
          return requestSent;
          
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
  public void networkDiscoveryNotification(InputStream is, String authorization) throws DiscoveryException {
   		// retrieve incoming message
  		StringBuilder inputStrBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				inputStrBuilder.append(line);
			}
		} catch (Exception e) {
            log.error("BufferedReader failed", e);
            throw new DiscoveryException(e.getMessage(), e);			
		}

  	    NetworkDiscoveryResponse ndResponse=null;

 	    Gson gson= new Gson();
        ndResponse= gson.fromJson(inputStrBuilder.toString(),NetworkDiscoveryResponse.class);
				
  		String requestId = ndResponse.getRequestId();
  		
  		for ( NetworkDiscoveryRspInfo entryNS: networkDiscoveryInfoList)
  		{
  			if (entryNS.getRequestId().equals(requestId)) {
  		    	// Update networkDiscoveryInfo  
			   List<NetworkDiscoveryResponse> responseList = entryNS.getNetworkDiscoveryRspList();
  				if ((responseList == null) || (responseList.isEmpty()) ){
  	               entryNS.setNetworkDiscoveryRspList(Arrays.asList(ndResponse));       	  	    
  				}
  				else {  					
  					responseList.add(ndResponse);
  					entryNS.setNetworkDiscoveryRspList(responseList); 
  				}

  	        if ( true == ndResponse.getAckFinalIndicator()) {
  	        	entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED);
  	        }  	        
  	            lock.lock();
  			    networkDiscoveryInfoList.add(entryNS); 
  			    lock.unlock();
  			}
  		}
 	    
  		  //obtain parent_request_id from request_id:
		  String[] requestIds = requestId.split(NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER);
		  String parent_requestId = requestIds[0];
  		
  		isReadyToResponseToServiceDecomposition (parent_requestId);
  	    
        return;
  	}
}