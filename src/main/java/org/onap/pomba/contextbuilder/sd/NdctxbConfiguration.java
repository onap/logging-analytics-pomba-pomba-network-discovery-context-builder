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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {

	@Autowired
    @Value("${nd.userId}")
    private String ndUserId;

    @Autowired
    @Value("${nd.password}")
    private String ndPassword;

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
        String auth = new String(this.ndUserId + ":" + this.ndPassword);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        return ("Basic " + new String(encodedAuth));
    }

    /* Network Discovery related */
	@Autowired
	@Value("${nd.host}")
	private String ndHost;

	@Autowired
	@Value("${nd.port}")
	private String ndPort;

	@Autowired
	@Value("${nd.httpProtocol}")
	private String httpNdProtocol;

	@Autowired
	@Value("${nd.networkDiscoveryPath}")
	private String networkDiscoveryPath;	

	@Autowired
	@Value("${nd.responseTimeOutInMilliseconds}")
	private String networkDiscoveryResponseTimeOutInMilliseconds;
	
	@Bean(name="ndBaseUrl")
	public String getNdURL() {
		String url = this.httpNdProtocol + "://" + this.ndHost + ":" + this.ndPort + this.networkDiscoveryPath;
        return url;
	}
	
	@Bean(name="ndResponseTimeOutInMilliseconds")
	public String getNdResponseTimeOutInMilliseconds() {
		String timeout = this.networkDiscoveryResponseTimeOutInMilliseconds;
        return timeout;
	}
	
    /* Network Discovery Context Builder related */
	@Autowired
	@Value("${ndcb.host}")
	private String ndcbHost;

	@Autowired
	@Value("${ndcb.port}")
	private String ndcbPort;

	@Autowired
	@Value("${ndcb.httpProtocol}")
	private String httpNdcbProtocol;

	@Autowired
	@Value("${ndcb.queryNetworkDiscoveryPath}")
	private String queryNetworkDiscoveryPath;	
		
	@Bean(name="ndcbCallBackUrl")
	public String getNdcbCallBackUrl() {
		String url = this.httpNdcbProtocol + "://" + this.ndcbHost + ":" + this.ndcbPort + this.queryNetworkDiscoveryPath;
        return url;
	}    
	
}
