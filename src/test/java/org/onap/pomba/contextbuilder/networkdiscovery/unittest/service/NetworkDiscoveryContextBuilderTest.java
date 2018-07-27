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

package org.onap.pomba.contextbuilder.networkdiscovery.unittest.service;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = { "sd.url=http://localhost:9505", "basicAuth.username=admin",
  "basicAuth.password=admin" })
public class NetworkDiscoveryContextBuilderTest {

 private String authorization = "Basic "
   + Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes(StandardCharsets.UTF_8));
 private String partnerName = "POMBA";
 private String transactionId = UUID.randomUUID().toString();
 private String serviceInstanceId = "c6456519-6acf-4adb-997c-3c363dd4caaf";

 HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
 NetworkDiscoveryNotification networkDiscoveryNotification = mock(NetworkDiscoveryNotification.class);

 @Autowired
 RestService restService;

 @Before
 public void setUp() throws Exception {
 }

 @After
 public void tearDown() throws Exception {
 }

 @Rule
 public WireMockRule serviceDecompMock = new WireMockRule(wireMockConfig().port(9505));

 @Rule
 public WireMockRule networkDiscoveryMock = new WireMockRule(wireMockConfig().port(9808));

 @Test
 public void testVerifyNoAuthoriztion() throws Exception {
  Response response = this.restService.getContext(httpServletRequest, null, partnerName, transactionId,
    serviceInstanceId, null, null);
  assertTrue(response.getEntity().toString().contains("Missing Authorization: "));
  assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
 }

 @Test
 public void testVerifyBadAuthoriztion() throws Exception {
  String authorization = "Basic "
    + Base64.getEncoder().encodeToString(("Test" + ":" + "Fake").getBytes(StandardCharsets.UTF_8));
  Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
    serviceInstanceId, null, null);
  assertEquals("Authorization Failed!", response.getEntity().toString());
  assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
 }

 @Test
 public void testVerifyPartnerName() throws Exception {
  Response response = this.restService.getContext(httpServletRequest, authorization, null, transactionId,
    serviceInstanceId, null, null);
  assertTrue(response.getEntity().toString().contains("X-ONAP-PartnerName"));
  assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
 }

 @Test
 public void testServiceInstanceId() throws Exception {
  Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId, null,
    null, null);
  assertTrue(response.getEntity().toString().contains("serviceInstanceId"));
  assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
 }

 @Test
 public void testVerifyGetContext() throws Exception {

  String networkDiscoveryCtxBuildUrl = "/network-discovery/service/context?serviceInstanceId=" + serviceInstanceId;

  addResponse(networkDiscoveryCtxBuildUrl, "junit/serviceDecomposition-1.json", serviceDecompMock);

  Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
    serviceInstanceId, null, null);

  assertEquals(Status.OK.getStatusCode(), response.getStatus());
 }

 @Test
 public void testUnauthorizedNetworkDiscoveryNotfi() throws Exception {

  String networkDiscoveryCallBackUrl = "/network-discovery/service/networkDiscoveryNotification";

  addResponse(networkDiscoveryCallBackUrl, "junit/networkDiscovery-1.json", networkDiscoveryMock);

  Response response = this.restService.networkDiscoveryNotification(networkDiscoveryNotification,
    networkDiscoveryCallBackUrl);

  assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
 }

 private void addResponse(String path, String classpathResource, WireMockRule thisMock) throws IOException {
  String payload = readFully(ClassLoader.getSystemResourceAsStream(classpathResource));
  thisMock.stubFor(get(path).willReturn(okJson(payload)));
 }

 private String readFully(InputStream in) throws IOException {
  char[] cbuf = new char[1024];
  StringBuilder content = new StringBuilder();
  try (InputStreamReader reader = new InputStreamReader(in, "UTF-8")) {
  int count;
  while ((count = reader.read(cbuf)) >= 0) {
  content.append(cbuf, 0, count);
  }
  }
  return content.toString();
 }
}
