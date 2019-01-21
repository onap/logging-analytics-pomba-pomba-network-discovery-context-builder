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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {

    // Network Discovery Context Builder Configuration values

    @Value("${networkDiscoveryCtxBuilder.httpProtocol}")
    private String httpNetworkDiscoveryCtxBuilderProtocol;

    @Value("${networkDiscoveryCtxBuilder.port}")
    private String networkDiscoveryCtxBuilderPort;

    @Value("${networkDiscoveryCtxBuilder.serviceName}")
    private String networkDiscoveryCtxBuilderServiceName;

    @Value("${networkDiscoveryCtxBuilder.userId:admin}")
    private String networkDiscoveryCtxBuilderUserId;

    @Value("${networkDiscoveryCtxBuilder.password:OBF:1u2a1toa1w8v1tok1u30}")
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

    @Value("${serviceDecomposition.password:OBF:1u2a1toa1w8v1tok1u30}")
    private String serviceDecompositionPassword;

    // Network Discovery Micro Service Configuration values

    @Value("${networkDiscoveryMicroService.host}")
    private String networkDiscoveryMicroServiceHost;

    @Value("${networkDiscoveryMicroService.port}")
    private String networkDiscoveryMicroServicePort;

    @Value("${networkDiscoveryMicroService.httpProtocol}")
    private String httpNetworkDiscoveryMicroServiceProtocol;

    @Value("${networkDiscoveryMicroService.networkDiscoveryPath}")
    private String networkDiscoveryMicroServicePath;

    @Value("${networkDiscoveryMicroService.responseTimeOutInMilliseconds}")
    private String networkDiscoveryResponseTimeOutInMilliseconds;

    @Value("${networkDiscoveryMicroService.userId:admin}")
    private String networkDiscoveryMicroServiceUserId;

    @Value("${networkDiscoveryMicroService.password:OBF:1u2a1toa1w8v1tok1u30}")
    private String networkDiscoveryMicroServicePassword;


    @Bean(name = "serviceDecompositionBaseUrl")
    public String getURL() {
        return this.serviceDecompositionHttpProtocol + "://" + this.serviceDecompositionHost + ":"
                + this.serviceDecompositionPort + this.serviceDecompositionServiceInstancePath;
    }

    @Bean(name = "serviceDecompositionBasicAuthorization")
    public String getSdBasicAuth() {
        String auth = this.serviceDecompositionUserId + ":" + Password.deobfuscate(this.serviceDecompositionPassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Bean(name = "networkDiscoveryCtxBuilderBasicAuthorization")
    public String getNdBasicAuth() {
        String auth = this.networkDiscoveryCtxBuilderUserId + ":"
                + Password.deobfuscate(this.networkDiscoveryCtxBuilderPassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Bean(name = "networkDiscoveryCtxBuilderBaseUrl")
    public String getNetworkDiscoveryCtxBuilderBaseUrl() {
        return this.httpNetworkDiscoveryCtxBuilderProtocol
                    + "://" + this.networkDiscoveryCtxBuilderServiceName
                    + ":"
                    + this.networkDiscoveryCtxBuilderPort;
    }

    @Bean(name = "networkDiscoveryMicroServiceBaseUrl")
    public String getNetworkDiscoveryURL() {
        return this.httpNetworkDiscoveryMicroServiceProtocol + "://" + this.networkDiscoveryMicroServiceHost + ":"
                + this.networkDiscoveryMicroServicePort + this.networkDiscoveryMicroServicePath;
    }

    @Bean(name = "networkDiscoveryResponseTimeOutInMilliseconds")
    public long getNdResponseTimeOutInMilliseconds() {
        return Integer.parseUnsignedInt(this.networkDiscoveryResponseTimeOutInMilliseconds);
    }

    @Bean(name = "networkDiscoveryMicroServiceHostAndPort")
    public String getNetworkDiscoveryMicroServiceHostAndPort() {
        return this.networkDiscoveryMicroServiceHost + ":" + this.networkDiscoveryMicroServicePort;
    }

    @Bean(name = "networkDiscoveryMicroServiceBasicAuthorization")
    public String getNetworkDiscoveryMicroServiceBasicAuth() {
        String auth = this.networkDiscoveryMicroServiceUserId + ":"
                + Password.deobfuscate(this.networkDiscoveryMicroServicePassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Autowired
    private Environment env;

}
