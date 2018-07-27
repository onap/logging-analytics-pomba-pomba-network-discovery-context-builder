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
package org.onap.pomba.contextbuilder.sd.util;

import javax.ws.rs.core.Response.Status;

import org.onap.pomba.contextbuilder.sd.exception.DiscoveryException;
import org.springframework.beans.factory.annotation.Autowired;

public class RestUtil {
    // Parameters for Query AAI Model Data API
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String HEADER_PARTNER_NAME = "X-ONAP-PartnerName";

    @Autowired
    private static String sdBasicAuthorization;

    /**
     * Validates the URL parameter seriveInstanceId.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public static void validateSeriveInstanceId(String serviceInstanceId) throws DiscoveryException {

        if (serviceInstanceId == null || serviceInstanceId.isEmpty())
            throw new DiscoveryException("Invalid request URL, missing parameter: " + SERVICE_INSTANCE_ID, Status.BAD_REQUEST);
    }

    /**
     * Validates the URL parameter X-ONAP-PartnerName.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public static void validatePartnerName(String partnerName) throws DiscoveryException {

        if ((partnerName == null) || partnerName.trim().isEmpty()) {
            throw new DiscoveryException("Missing header parameter: " + HEADER_PARTNER_NAME, Status.BAD_REQUEST);
        }
    }

    /**
     * Validates the Basic authorization header as admin:admin.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public static void validateBasicAuth(String authorization) throws DiscoveryException {

        if (authorization != null && !authorization.trim().isEmpty() && authorization.startsWith("Basic")) {
            /*
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            // credentials = username:password
            final String[] values = credentials.split(":",2);
            if (!values[0].equals(BASIC_AUTH_ID) || !values[1].equals(BASIC_AUTH_PW)) {
                throw new DiscoveryException("Authorization Failed", Status.BAD_REQUEST);
            }
            */
            if (!authorization.equals(sdBasicAuthorization));
        } else {
            throw new DiscoveryException("Missing Authorization: " +(authorization==null ? "null" : authorization.toString()), Status.BAD_REQUEST);
        }
    }


}
