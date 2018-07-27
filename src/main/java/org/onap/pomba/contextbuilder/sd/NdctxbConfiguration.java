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
package org.onap.pomba.contextbuilder.sd;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class NdctxbConfiguration {

    @Autowired
    @Value("${networkDiscovery.userId}")
    private String networkDiscoveryUserId;

    @Autowired
    @Value("${networkDiscovery.password}")
    private String networkDiscoveryPassword;

    @Autowired
    @Value("${sd.host}")
    private String host;

    @Autowired
    @Value("${sd.port}")
    private String port;

    @Autowired
    @Value("${sd.httpProtocol}")
    private String httpProtocol;

    @Autowired
    @Value("${sd.serviceInstancePath}")
    private String serviceInstancePath;

    @Autowired
    @Value("${sd.userId}")
    private String sdUserId;

    @Autowired
    @Value("${sd.password}")
    private String sdPassword;

    @Bean(name="sdBaseUrl")
    public String getURL() {
        String url = this.httpProtocol + "://" + this.host + ":" + this.port + this.serviceInstancePath;
        return url;
    }

    @Bean(name="sdBasicAuthorization")
    public String getSdBasicAuth() {
        String auth = new String(this.sdUserId + ":" + this.sdPassword);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        return ("Basic " + new String(encodedAuth));
    }

    @Bean(name="ndBasicAuthorization")
    public String getNdBasicAuth() {
        String auth = new String(this.networkDiscoveryUserId + ":" + this.networkDiscoveryPassword);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        return ("Basic " + new String(encodedAuth));
    }

    /* Network Discovery related */
	@Autowired
	@Value("${networkDiscovery.host}")
	private String networkDiscoveryHost;

	@Autowired
	@Value("${networkDiscovery.port}")
	private String networkDiscoveryPort;

	@Autowired
	@Value("${networkDiscovery.httpProtocol}")
	private String httpNetworkDiscoveryProtocol;

	@Autowired
	@Value("${networkDiscovery.networkDiscoveryPath}")
	private String networkDiscoveryPath;	

	@Autowired
	@Value("${nd.responseTimeOutInMilliseconds}")
	private String networkDiscoveryResponseTimeOutInMilliseconds;
	
	@Bean(name="networkDiscoveryBaseUrl")
	public String getNetworkDiscoveryURL() {
		String url = this.httpNetworkDiscoveryProtocol + "://" + this.networkDiscoveryHost + ":" + this.networkDiscoveryPort + this.networkDiscoveryPath;
        return url;
	}
	
	@Bean(name="ndResponseTimeOutInNanoseconds")
	public long  getNdResponseTimeOutInNanoseconds() {
    	// Convert to nanosecond (1 second = 1000 milliseconds = 1,000,000,000 nanoseconds)
    	int timeoutV = Integer.parseInt(this.networkDiscoveryResponseTimeOutInMilliseconds);                	
        long timeOut = TimeUnit.MILLISECONDS.toNanos(timeoutV);		
        return timeOut;
	}
	
	
	
    /* Network Discovery Context Builder related */
	@Autowired
	@Value("${networkDiscoveryCtxBuilder.port}")
	private String networkDiscoveryCtxBuilderPort;

	@Autowired
	@Value("${networkDiscoveryCtxBuilder.httpProtocol}")
	private String httpNetworkDiscoveryCtxBuilderProtocol;	
		
	@Bean(name="networkDiscoveryCtxBuilderBaseUrl")
	public String getNetworkDiscoveryCtxBuilderBaseUrl() {
		String url = this.httpNetworkDiscoveryCtxBuilderProtocol + "://" + "localhost" + ":" + this.networkDiscoveryCtxBuilderPort;
		
		return url;
	}    
	
}
