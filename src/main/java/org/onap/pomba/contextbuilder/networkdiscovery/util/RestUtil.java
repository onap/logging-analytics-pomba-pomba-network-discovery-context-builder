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
package org.onap.pomba.contextbuilder.networkdiscovery.util;

import javax.ws.rs.core.Response.Status;

import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.ErrorMessage;

public class RestUtil {
    // Parameters for Query AAI Model Data API
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String HEADER_PARTNER_NAME = "X-ONAP-PartnerName";

    /**
     * Validates the URL parameter seriveInstanceId.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public static void validateSeriveInstanceId(String serviceInstanceId) throws DiscoveryException {

        if (serviceInstanceId == null || serviceInstanceId.trim().isEmpty())
            throw new DiscoveryException(ErrorMessage.INVALID_REQUEST_URL + ErrorMessage.MISSING_PARAMTER + SERVICE_INSTANCE_ID, Status.BAD_REQUEST);
    }

    /**
     * Validates the URL parameter X-ONAP-PartnerName.
     *
     * @throws DiscoveryException if there is missing parameter
     */
    public static void validatePartnerName(String partnerName) throws DiscoveryException {

        if ((partnerName == null) || partnerName.trim().isEmpty()) {
            throw new DiscoveryException(ErrorMessage.MISSING_PARAMTER + HEADER_PARTNER_NAME, Status.BAD_REQUEST);
        }
    }


}
