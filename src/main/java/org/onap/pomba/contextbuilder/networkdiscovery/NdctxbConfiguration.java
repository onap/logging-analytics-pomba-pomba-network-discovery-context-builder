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

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.eclipse.jetty.util.security.Password;
import org.onap.pomba.contextbuilder.networkdiscovery.exception.DiscoveryException;
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
    private final String prefixResourceType = "networkDiscoveryCtxBuilder.resourceTypeMapping.";
    private final String whiteSpace = "\\s";

    // Network Discovery Context Builder Configuration values

    @Value("${networkDiscoveryCtxBuilder.httpProtocol}")
    private String httpNetworkDiscoveryCtxBuilderProtocol;

    @Value("${networkDiscoveryCtxBuilder.port}")
    private String networkDiscoveryCtxBuilderPort;

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
        String url = this.serviceDecompositionHttpProtocol + "://" + this.serviceDecompositionHost + ":"
                + this.serviceDecompositionPort + this.serviceDecompositionServiceInstancePath;
        return url;
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
    public String getNetworkDiscoveryCtxBuilderBaseUrl() throws DiscoveryException {
        String url = null;
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            url = this.httpNetworkDiscoveryCtxBuilderProtocol + "://" + localIp + ":"
                    + this.networkDiscoveryCtxBuilderPort;
        } catch (Exception e) {
            log.error("Unable to obtain localIp: " + e.getMessage());
            throw new DiscoveryException(e.getMessage(), e);
        }

        return url;
    }

    @Bean(name = "networkDiscoveryCtxBuilderResources")
        public String getNetworkDiscoveryCtxBuilderResourcs() {
            return this.networkDiscoveryCtxBuilderResourceList;
    }


    @Bean(name = "networkDiscoveryMicroServiceBaseUrl")
    public String getNetworkDiscoveryURL() {
        String url = this.httpNetworkDiscoveryMicroServiceProtocol + "://" + this.networkDiscoveryMicroServiceHost + ":"
                + this.networkDiscoveryMicroServicePort + this.networkDiscoveryMicroServicePath;
        return url;
    }

    @Bean(name = "networkDiscoveryResponseTimeOutInMilliseconds")
    public long getNdResponseTimeOutInMilliseconds() {
        long timeoutV = Integer.parseUnsignedInt(this.networkDiscoveryResponseTimeOutInMilliseconds);
        return timeoutV;
    }

    @Bean(name = "networkDiscoveryMicroServiceHostAndPort")
    public String getNetworkDiscoveryMicroServiceHostAndPort() {
        String hostNPort = this.networkDiscoveryMicroServiceHost + ":" + this.networkDiscoveryMicroServicePort;
        return hostNPort;
    }

    @Bean(name = "networkDiscoveryMicroServiceBasicAuthorization")
    public String getNetworkDiscoveryMicroServiceBasicAuth() {
        String auth = this.networkDiscoveryMicroServiceUserId + ":"
                + Password.deobfuscate(this.networkDiscoveryMicroServicePassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Autowired
    private Environment env;
    
    @Bean(name = "networkDiscoveryCtxBuilderResourceTypeMapping")
    public Map<String, String> getResourceTypeMapping() {
        Map<String, String> props = new HashMap<>();
        MutablePropertySources propSrcs = ((AbstractEnvironment) this.env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .forEach(propName -> {
                    if (propName.startsWith(prefixResourceType)) {
                        String myKey = propName.substring(prefixResourceType.length()).replaceAll(whiteSpace,"");
                        String myValue = this.env.getProperty(propName).replaceAll(whiteSpace, "");
                        props.put( myKey , myValue);
                    }
                });

        log.info(props.toString());
        return props;
    }    
}
