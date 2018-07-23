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

package org.onap.pomba.contextbuilder.networkdiscovery;

import java.util.Base64;
import org.eclipse.jetty.util.security.Password;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {

    // Network Discover Configuration values

    @Value("${networkDiscoveryCtxBuilder.userId:admin}")
    private String networkDiscoveryCtxBuilderUserId;

    @Value("${networkDiscoveryCtxBuilder.password:admin}")
    private String networkDiscoveryCtxBuilderPassword;

    // Service Decomposition Configuration values

    @Value("${serviceDecomposition.host:127.0.0.1}")
    private String serviceDecompositionHost;

    @Value("${serviceDecomposition.port}")
    private String serviceDecompositionPort;

    @Value("${serviceDecomposition.httpProtocol}")
    private String serviceDecompositionHttpProtocol;

    @Value("${serviceDecomposition.serviceInstancePath}")
    private String serviceDecompositionServiceInstancePath;

    @Value("${serviceDecomposition.userId:admin}")
    private String serviceDecompositionUserId;

    @Value("${serviceDecomposition.password:admin}")
    private String serviceDecompositionPassword;

    @Bean(name="serviceDecompositionBaseUrl")
    public String getURL() {
        String url = this.serviceDecompositionHttpProtocol + "://"
                + this.serviceDecompositionHost + ":"
                + this.serviceDecompositionPort
                + this.serviceDecompositionServiceInstancePath;
        return url;
    }

    @Bean(name="serviceDecompositionBasicAuthorization")
    public String getSdBasicAuth() {
        String auth = new String(this.serviceDecompositionUserId + ":" + Password.deobfuscate(this.serviceDecompositionPassword));
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Bean(name="networkDiscoveryCtxBuilderBasicAuthorization")
    public String getNdBasicAuth() {
        String auth = this.networkDiscoveryCtxBuilderUserId + ":" + Password.deobfuscate(this.networkDiscoveryCtxBuilderPassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

}
