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
package org.onap.pomba.contextbuilder.nd;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {

    @Value("${nd.userId:admin}")
    private String ndUserId;

    @Value("${nd.password:admin}")
    private String ndPassword;

    @Value("${sd.host:127.0.0.1}")
    private String host;

    @Value("${sd.port}")
    private String port;

    @Value("${sd.httpProtocol}")
    private String httpProtocol;

    @Value("${sd.serviceInstancePath}")
    private String serviceInstancePath;

    @Value("${sd.userId:admin}")
    private String sdUserId;

    @Value("${sd.password:admin}")
    private String sdPassword;

    @Bean(name="sdBaseUrl")
    public String getURL() {
        String url = this.httpProtocol + "://" + this.host + ":" + this.port + this.serviceInstancePath;
        return url;
    }

    @Bean(name="sdBasicAuthorization")
    public String getSdBasicAuth() {
        String auth = new String(this.sdUserId + ":" + this.sdPassword);
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }

    @Bean(name="ndBasicAuthorization")
    public String getNdBasicAuth() {
        String auth = this.ndUserId + ":" + this.ndPassword;
        return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
    }


}
