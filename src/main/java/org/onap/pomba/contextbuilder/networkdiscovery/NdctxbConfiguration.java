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
import java.util.Base64;

import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NdctxbConfiguration {
 private static Logger log = LoggerFactory.getLogger(NdctxbConfiguration.class);

 // Network Discover Configuration values

 @Value("${nd.userId:admin}")
 private String ndUserId;

 @Value("${nd.password:admin}")
 private String ndPassword;

 // Service Decomposition Configuration values

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

 @Bean(name = "sdBaseUrl")
 public String getURL() {
  String url = this.httpProtocol + "://" + this.host + ":" + this.port + this.serviceInstancePath;
  return url;
 }

 @Bean(name = "sdBasicAuthorization")
 public String getSdBasicAuth() {
  String auth = new String(this.sdUserId + ":" + this.sdPassword);
  return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
 }

 @Bean(name = "ndBasicAuthorization")
 public String getNdBasicAuth() {
  String auth = this.ndUserId + ":" + this.ndPassword;
  return ("Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
 }

 /* Network Discovery related */
 @Value("${networkDiscovery.host}")
 private String networkDiscoveryHost;

 @Value("${networkDiscovery.port}")
 private String networkDiscoveryPort;

 @Value("${networkDiscovery.httpProtocol}")
 private String httpNetworkDiscoveryProtocol;

 @Value("${networkDiscovery.networkDiscoveryPath}")
 private String networkDiscoveryPath;

 @Value("${networkDiscovery.responseTimeOutInMilliseconds}")
 private String networkDiscoveryResponseTimeOutInMilliseconds;

 @Bean(name = "networkDiscoveryBaseUrl")
 public String getNetworkDiscoveryURL() {
  String url = this.httpNetworkDiscoveryProtocol + "://" + this.networkDiscoveryHost + ":" + this.networkDiscoveryPort
    + this.networkDiscoveryPath;
  return url;
 }

 @Bean(name = "ndResponseTimeOutInMilliseconds")
 public long getNdResponseTimeOutInMilliseconds() {
  long timeoutV = Integer.parseUnsignedInt(this.networkDiscoveryResponseTimeOutInMilliseconds);
  return timeoutV;
 }

 /* Network Discovery Context Builder related */
 @Value("${server.port:8080}")
 private int networkDiscoveryCtxBuilderPort;

 @Value("${networkDiscoveryCtxBuilder.httpProtocol}")
 private String httpNetworkDiscoveryCtxBuilderProtocol;

 @Bean(name = "networkDiscoveryCtxBuilderPort")
 public String getNetworkDiscoveryCtxBuilderPort() {
  return Integer.toString(networkDiscoveryCtxBuilderPort);
 }

 @Bean(name = "networkDiscoveryCtxBuilderBaseUrl")
 public String getNetworkDiscoveryCtxBuilderBaseUrl() {
  String url = null;
  try {
  String localIp = InetAddress.getLocalHost().getHostAddress();
  url = this.httpNetworkDiscoveryCtxBuilderProtocol + "://" + localIp + ":" + getNetworkDiscoveryCtxBuilderPort();
  } catch (Exception e) {
  log.error("Unable to obtain localIp: " + e.getMessage());
  }

  // testing,

  return url;
 }

 @Bean(name = "jerseyClient")
 public Client getJerseyClient() {
  JerseyConfiguration jerseyConfiguration = new JerseyConfiguration();
  Client client = jerseyConfiguration.jerseyClient();
  return client;
 }
}
