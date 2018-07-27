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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.onap.pomba.contextbuilder.sd.model.NetworkDiscoveryRspInfo;
import org.onap.pomba.contextbuilder.sd.util.SignalBarrier;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;

import com.google.gson.Gson;
import javax.ws.rs.client.Client;
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
    public static String NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER = "_";


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
    
    @Autowired
    private String sdBaseUrl;

    @Autowired
    private String ndBasicAuthorization;

    @Autowired
    private String sdBasicAuthorization;

    @Autowired
    @SuppressWarnings("unused")
    private String ndBaseUrl;

    @Autowired
    @SuppressWarnings("unused")
    private String ndcbCallBackUrl;

    @Autowired
    @SuppressWarnings("unused")
    private String ndResponseTimeOutInMilliseconds;

    private List<NetworkDiscoveryRspInfo> networkDiscoveryInfoList;
    private SignalBarrier barrier = null;
    private int internalIdx = 0;

    @javax.annotation.Resource
    private Client callbackClient;

       @javax.annotation.Resource
       private Map<String, String> enricherTypeURLs;

    public SpringServiceImpl() {
        networkDiscoveryInfoList = new ArrayList<>();
    }
    
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
                //Wait for all network discovery responses are received, then update ServiceDecompCtx
                if (barrier == null) {
                  barrier = new SignalBarrier();
                }

                try {
                	// Convert to nanosecond (1 second = 1000 milliseconds = 1,000,000,000 nanoseconds)
                	int timeoutV = Integer.parseInt(this. ndResponseTimeOutInMilliseconds);                	
                    long remaindingTimeOut = TimeUnit.MILLISECONDS.toNanos(timeoutV);
                    while ((!isReadyToResponseToServiceDecomposition()) && (remaindingTimeOut > 0)) {
                    	remaindingTimeOut = barrier.awaitNanos(remaindingTimeOut);                    
                    }
                    
                    barrier = null;
                    return updateServiceDecompCtx (serviceDecompCtx);
                    
                } catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
                }                                  	
            } else {
            	return serviceDecompCtx;
            }
            } finally {
            MDC.clear();
        }
        return null;
    }

    private ModelContext updateServiceDecompCtx (ModelContext serviceDecompCtx ) {
  	 //TO DO: We can’t add network discovery data to serviceDecompCtx because the existing “v0” context aggregator 
  	 //context model doesn’t support it.  We will have to wait for the real “v1” context 
       //model which contains attributes, vservers and networks.

      // After update ServiceDecompCtx, we need to delete the existing networkDiscoveryInfoList
      networkDiscoveryInfoList.clear();
  	  return serviceDecompCtx;
    }
    private boolean isReadyToResponseToServiceDecomposition () {
  	  for ( NetworkDiscoveryRspInfo entry : networkDiscoveryInfoList) {
  	  	if (!(entry.getStatus().equals(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED)) ){
  	  		return false;
  	  	}       	  	
  	  }  
  	  
  	  // When it comes here, all responses were received, then we will exit the wait in the calling method.
  	  if (barrier != null) {
  	  	  barrier.signal();  		  
  	  }
  	  return true;
    }
    
  	  //TO DO: We can’t add network discovery data to serviceDecompCtx because the existing “v0” context aggregator 
       //context model doesn’t support it.  We will have to wait for the real “v1” context 
  	  //model which contains attributes, vservers and networks.

    // Return true when message is sent to network-discovery microService, otherwise, return false. 
    private boolean sendNetworkDiscoveryRequest (ModelContext serviceDecompCtx, String serviceInstanceId , String partner_name, String authorization)  throws DiscoveryException{
    log.info("Http Request", "GET");
     boolean messageSent = false;  
     String old_requestId = MDC.get(MDC_REQUEST_ID);
            
     List <VF> vfList = serviceDecompCtx.getVf();
     
     for (VF entryVF: vfList){
    	 
     	List <VNFC> vnfcList = entryVF.getVnfc();
     	for (VNFC entryVnfc : vnfcList){
       		String vserverId = entryVnfc.getUuid();  
       		
     		//String requestId = old_requestId + NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER + UUID.randomUUID().toString();            		
      		internalIdx++;
      		String requestId = old_requestId + NETWORK_DISCOVERY_RSP_REQUESTID_SPLITTER + internalIdx;            		

     		if ( true == sendNetworkDiscoveryRequestToSpecificVserver(partner_name, authorization,old_requestId, requestId, vserverId, RESOURCE_TYPE_VSERVER)) {
     			messageSent = true;
     		}

       	}    	 
         }
     return messageSent;
    }	

      // Return true when message is sent to network-discovery microService, otherwise, return false.
      private boolean sendNetworkDiscoveryRequestToSpecificVserver (String partner_name, String authorization, String old_requestId, String requestId,String vserverId,String resourceType ) throws DiscoveryException{
  	    boolean requestSent = false;
  	    String callbackUrlStr = ndcbCallBackUrl;
  	    String networkDiscoveryUrl = getNetworkDiscoveryUrl(resourceType, requestId, callbackUrlStr, vserverId );
  	    
      	NetworkDiscoveryRspInfo entryNS = new NetworkDiscoveryRspInfo();
     		entryNS.setRequestId(requestId);
     		entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_REQUEST_SENT);
     		entryNS.setvServerId(vserverId);

     		// send message to Network Discovery API
     		
          HttpURLConnection conn = null;
  		try {
  			  URL url = new URL( networkDiscoveryUrl );
  			  conn = (HttpURLConnection) url.openConnection();
  			  conn.setRequestMethod("GET");
  			  conn.setRequestProperty("Content-Type", "application/json");
      		  conn.setRequestProperty("Accept", "application/json");
      		  conn.setRequestProperty("Authorization", authorization);
      		  conn.setRequestProperty("X-ONAP-PartnerName", partner_name);    		  
      		  conn.setRequestProperty("X-ONAP-RequestID", old_requestId);
      		  conn.setRequestProperty("requestId", requestId);
      		  conn.setRequestProperty("resourceType", resourceType);
      		  conn.setRequestProperty("resourceId", vserverId);
      		  conn.setRequestProperty("notificationURL", callbackUrlStr);

  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
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
    	        
      		    networkDiscoveryInfoList.add(entryNS);    	        	
  				requestSent = true;    	        

  			}
  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
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

	private String getNetworkDiscoveryUrl (String resourceType, String requestId, String notificationURL, String resourceId) throws DiscoveryException {
		return new String(ndBaseUrl
				+ "?resourceType=" + resourceType
				+ "&requestId=" + requestId
				+ "&notificationURL=" + notificationURL
				+ "&resourceId=" + resourceId
             );
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
  public NetworkDiscoveryResponse queryNetworkDiscovery(InputStream is, String authorization) throws DiscoveryException {
   		// retrieve incoming message
  		StringBuilder inputStrBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				inputStrBuilder.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
  				if (responseList == null){
  	               entryNS.setNetworkDiscoveryRspList(Arrays.asList(ndResponse));       	  	    
  				} else if (responseList.isEmpty()){
   	               entryNS.setNetworkDiscoveryRspList(Arrays.asList(ndResponse));       	  	      					
  				}
  				else {  					
  					responseList.add(ndResponse);
  					entryNS.setNetworkDiscoveryRspList(responseList); 
  				}

  	        if ( true == ndResponse.getAckFinalIndicator()) {
  	        	entryNS.setStatus(NETWORK_DISCOVERY_RSP_STATE_RSP_ALL_RECEIVED);
  	        }  	        
  			    networkDiscoveryInfoList.add(entryNS);   				
  			}
  		}
 	    
  		isReadyToResponseToServiceDecomposition ();
  	    
        return ndResponse;
  	}
}