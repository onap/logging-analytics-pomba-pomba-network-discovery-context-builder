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
package org.onap.pomba.contextbuilder.networkdiscovery.service.rs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.pomba.common.datatypes.ModelContext;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.ErrorMessage;
import org.onap.pomba.contextbuilder.networkdiscovery.service.SpringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestServiceImpl implements RestService {
    private static Logger log = LoggerFactory.getLogger(RestService.class);
    private static final String EMPTY_JSON_OBJECT = "{}";
    private static final String HEADER_REQUEST_ID = "X-ONAP-RequestID";

    @Autowired
    private SpringService service;

    @Override
    public Response getContext(HttpServletRequest req, String authorization, String partnerName, String requestId, String fromAppId, String transactionId,
            String serviceInstanceId, String modelVersionId, String modelInvariantId) throws DiscoveryException {

        // Do some validation on Http headers and URL parameters

    	//The request ID in the header is not yet standardized to X-ONAP-RequestID.  We would still support X-TransactionId until further notice.
    	if(requestId == null || requestId.isEmpty()) {
    		if(transactionId != null) {
    			requestId = transactionId;
    		} else {
    			requestId = UUID.randomUUID().toString();
    			log.debug("{} is missing; using newly generated value: {}", HEADER_REQUEST_ID, requestId);
    		}
    	}

    	//The partner name in the header is not yet standardized to X-PartnerName.  We would still support X-FromAppId until further notice.
    	if(partnerName == null || partnerName.isEmpty()) {
    		if(fromAppId != null) {
    			partnerName = fromAppId;
    		}
    	}

        try {
            ModelContext sdContext = service.getContext(req, partnerName, authorization, requestId, serviceInstanceId,
                    modelVersionId, modelInvariantId);
            if (sdContext == null) {
                // Return empty JSON
                return Response.ok().entity(EMPTY_JSON_OBJECT).build();
            } else {
                Gson gson = new GsonBuilder().create();
                return Response.ok().entity(gson.toJson(sdContext)).build();
            }
        } catch (DiscoveryException x) {
            log.error(ErrorMessage.CONTEXT_BUILDER_FAILED, x);
            return Response.status(x.getHttpStatus()).entity(x.getMessage()).build();

        } catch (Exception x) {
            log.error(ErrorMessage.CONTEXT_BUILDER_FAILED, x);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(x.getMessage()).build();
        }
    }

}
