/*
 * ============LICENSE_START=================================================== Copyright (c) 2018
 * Amdocs ============================================================================ Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. ============LICENSE_END=====================================================
 */

package org.onap.pomba.contextbuilder.networkdiscovery.unittest.service;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.google.gson.Gson;

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
import org.onap.pomba.common.datatypes.ModelContext;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {"serviceDecomposition.host=localhost", "serviceDecomposition.port=3333",
        "networkDiscoveryMicroService.host=localhost", "networkDiscoveryMicroService.port=9808",
        "networkDiscoveryMicroService.httpProtocol=http",
        "networkDiscoveryMicroService.responseTimeOutInMilliseconds=1000"})
public class NetworkDiscoveryContextBuilderTest {

    private String authorization =
            "Basic " + Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes(StandardCharsets.UTF_8));
    private String partnerName = "POMBA";
    private String transactionId = UUID.randomUUID().toString();
    private String serviceInstanceId = "c6456519-6acf-4adb-997c-3c363dd4caaf";

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

    @Autowired
    Environment environment;

    @Autowired
    RestService restService;

    @Rule
    public WireMockRule serviceDecompositionRule = new WireMockRule(wireMockConfig().port(3333));
    @Rule
    public WireMockRule networkDiscoveryMicroServiceRule = new WireMockRule(wireMockConfig().port(9808));

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testVerifyNoAuthoriztion() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, null, partnerName, transactionId, null,
                null, serviceInstanceId, null, null);
        assertTrue(response.getEntity().toString().contains("Missing Authorization: "));
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyBadAuthoriztion() throws Exception {
        String authorization =
                "Basic " + Base64.getEncoder().encodeToString(("Test" + ":" + "Fake").getBytes(StandardCharsets.UTF_8));
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, serviceInstanceId, null, null);
        assertEquals("Authorization Failed", response.getEntity().toString());
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyPartnerName() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, authorization, null, transactionId, null,
                null, serviceInstanceId, null, null);
        assertTrue(response.getEntity().toString().contains("X-ONAP-PartnerName"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyServiceInstanceId() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, null, null, null);
        assertTrue(response.getEntity().toString().contains("serviceInstanceId"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyServiceDecomposition() throws Exception {

        String urlStr = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(urlStr, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        addResponseAny("junit/networkDiscoveryResponseVserver-1.json", networkDiscoveryMicroServiceRule);
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, serviceInstanceId, null, null);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyGetContext() throws Exception {

        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);

        String vserverPayload = readFully(
                ClassLoader.getSystemResourceAsStream("junit/networkDiscoveryResponseVserver-1.json"));
        networkDiscoveryMicroServiceRule.stubFor(WireMock
                .any(WireMock.urlPathEqualTo("/network-discovery/v1/network/resource"))
                .withQueryParam("resourceType", WireMock.equalTo("vserver")).willReturn(okJson(vserverPayload)));

        String l3networkPayload = readFully(
                ClassLoader.getSystemResourceAsStream("junit/networkDiscoveryResponseL3Network.json"));
        networkDiscoveryMicroServiceRule.stubFor(WireMock
                .any(WireMock.urlPathEqualTo("/network-discovery/v1/network/resource"))
                .withQueryParam("resourceType", WireMock.equalTo("l3-network")).willReturn(okJson(l3networkPayload)));

        String pInterfacePayload = readFully(
                ClassLoader.getSystemResourceAsStream("junit/networkDiscoveryResponsePinterface.json"));
        networkDiscoveryMicroServiceRule.stubFor(WireMock
                .any(WireMock.urlPathEqualTo("/network-discovery/v1/network/resource"))
                .withQueryParam("resourceType", WireMock.equalTo("port")).willReturn(okJson(pInterfacePayload)));

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, serviceInstanceId, null, null);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        System.out.println(response.getEntity());

        Gson gson = new Gson();
        ModelContext modelContext = gson.fromJson((String) response.getEntity(), ModelContext.class);
        assertTrue(modelContext.getVnfs().size() > 0);
        assertTrue(modelContext.getVnfs().get(0).getVfModules().size() > 0);
        assertTrue(modelContext.getVnfs().get(0).getVfModules().get(0).getVms().size() > 0);

    }

    @Test
    public void testVerifyGetContextNdResourceNotFound() throws Exception {

        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        UrlPattern testPath = WireMock.anyUrl();
        networkDiscoveryMicroServiceRule.stubFor(get(testPath).willReturn(WireMock.notFound()));

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, serviceInstanceId, null, null);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        Gson gson = new Gson();
        ModelContext modelContext = gson.fromJson((String)response.getEntity(), ModelContext.class);
        assertTrue(modelContext.getVnfs().size() > 0);
        assertTrue(modelContext.getVnfs().get(0).getVfModules().size() > 0);
        assertTrue(modelContext.getVnfs().get(0).getVfModules().get(0).getVms().size() > 0);

    }

    @Test
    public void testVerifyGetContextSdResoureNofFound() throws Exception {

        UrlPattern testPath = WireMock.anyUrl();
        serviceDecompositionRule.stubFor(get(testPath).willReturn(WireMock.notFound()));

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, serviceInstanceId, null, null);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyNoPartnerNameWithFromAppId() throws Exception {
        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        addResponseAny("junit/networkDiscoveryResponseVserver-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.getContext(httpServletRequest, authorization, null, transactionId,
                partnerName, null, serviceInstanceId, null, null);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyNoRequestIdNoTransactionId() throws Exception {
        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        addResponseAny("junit/networkDiscoveryResponseVserver-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, null, null,
                null, serviceInstanceId, null, null);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyNoPartnerNameNoFromAppId() throws Exception {
        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        addResponseAny("junit/networkDiscoveryResponseVserver-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.getContext(httpServletRequest, authorization, null, transactionId, null,
                null, serviceInstanceId, null, null);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyNoRequestIdWithTransactionId() throws Exception {
        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecompositionResponse-1.json", serviceDecompositionRule);
        addResponseAny("junit/networkDiscoveryResponseVserver-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, null, null,
                transactionId, serviceInstanceId, null, null);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    private void addResponse(String path, String classpathResource, WireMockRule thisMock) throws IOException {
        String payload = readFully(ClassLoader.getSystemResourceAsStream(classpathResource));
        thisMock.stubFor(get(path).willReturn(okJson(payload)));
    }

    private void addResponseAny(String classpathResource, WireMockRule thisMock) throws IOException {
        String payload = readFully(ClassLoader.getSystemResourceAsStream(classpathResource));
        UrlPattern testPath = WireMock.anyUrl();
        thisMock.stubFor(get(testPath).willReturn(okJson(payload)));
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
