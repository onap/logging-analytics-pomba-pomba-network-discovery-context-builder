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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
public interface RestService {

    @GET
    @Path("/context")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Fetch network info for service",
            notes = "Returns a JSON object which represents the Context model data",
            response = Response.class
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Bad Request"),
                    @ApiResponse(code = 404, message = "Service not available"),
                    @ApiResponse(code = 500, message = "Unexpected Runtime error")
                    })
    public Response getContext(
            @Context HttpServletRequest req,
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("X-ONAP-PartnerName") String xpartnerName,
            @HeaderParam("X-ONAP-RequestID") String xRequestId,
            @QueryParam("serviceInstanceId") String serviceInstanceId,
            @QueryParam("modelVersionId") String modelVersionId,
            @QueryParam("modelInvariantId") String modelInvariantId) throws DiscoveryException;

	@POST
	@Path("/networkDiscoveryNotification")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Send query request to Network Discovery", notes = "Retrieve information from primary data sources", response = NetworkDiscoveryResponse.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Request has completed and no more information is forthcoming."),
			@ApiResponse(code = 400, message = "Missing mandatory field in the request or HTTP header."),
			@ApiResponse(code = 500, message = "Request failed due to internal error") })
	public Response networkDiscoveryNotification(NetworkDiscoveryNotification notification,
			@HeaderParam("Authorization") String authorization) throws DiscoveryException;
}