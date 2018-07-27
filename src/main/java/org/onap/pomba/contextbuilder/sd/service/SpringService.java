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

import org.onap.pomba.auditcommon.datatypes.ModelContext;
import org.onap.pomba.contextbuilder.sd.exception.DiscoveryException;
import org.onap.pomba.contextbuilder.sd.model.HelloWorld;
import java.io.InputStream;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;


public interface SpringService {
    public HelloWorld getQuickHello(String name);

    public ModelContext getContext(String partnerName,
                              String authorization,
                              String transactionId,
                              String serviceInstanceId,
                              String modelVersionId,
                              String modelInvariantId) throws DiscoveryException;

    public void validateBasicAuth(String authorization) throws DiscoveryException;

    public void networkDiscoveryNotification(NetworkDiscoveryNotification is,
  			String authorization 
    )throws DiscoveryException; 

}
