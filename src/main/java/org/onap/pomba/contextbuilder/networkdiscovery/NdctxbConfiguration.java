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

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.eclipse.jetty.util.security.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {
    private static Logger log = LoggerFactory.getLogger(NdctxbConfiguration.class);
    private static final String PREFIX_RESOURCE_TYPE = "networkDiscoveryCtxBuilder.resourceTypeMapping.";
    private static final String WHITE_SPACE = "\\s";

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

    @Value("${networkDiscoveryCtxBuilder.resourceList:vnfcs}")
    private String networkDiscoveryCtxBuilderResourceList;


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

    @Bean(name = "networkDiscoveryCtxBuilderResources")
    public String getNetworkDiscoveryCtxBuilderResourcs() {
            return this.networkDiscoveryCtxBuilderResourceList;
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

    // This method builds a map between Service Decomposition resource type and Network Discovery
    // Context Builder resource type using dynamic mapping technique.
    // It scans the contents of the configuration file "application.properties",
    // searching for the string "networkDiscoveryCtxBuilder.resourceTypeMapping.", and if found,
    // anything from the remaining string will be used as the key (Service Decomposition resource Type)
    // to match to the value of assignment (network discovery context builder resource type).
    // For example,"networkDiscoveryCtxBuilder.resourceTypeMapping.BBB = bbb",
    // Service Decomposition resource type BBB matches to context builder resource type bbb
    @Bean(name = "networkDiscoveryCtxBuilderResourceTypeMapping")
    public Map<String, String> getResourceTypeMapping() {
        Map<String, String> props = new HashMap<>();
        MutablePropertySources propSrcs = ((AbstractEnvironment) this.env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .forEach(propName -> {
                    if (propName.startsWith(PREFIX_RESOURCE_TYPE)) {
                        String myKey = propName.substring(PREFIX_RESOURCE_TYPE.length()).replaceAll(WHITE_SPACE,"");
                        String myValue = this.env.getProperty(propName).replaceAll(WHITE_SPACE, "");
                        props.put( myKey , myValue);
                    }
                });

        log.info(props.toString());
        return props;
    }
}
