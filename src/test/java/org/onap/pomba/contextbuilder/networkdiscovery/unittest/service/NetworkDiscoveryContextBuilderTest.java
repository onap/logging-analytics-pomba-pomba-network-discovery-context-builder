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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NetworkDiscoveryRspInfo;
import org.onap.pomba.contextbuilder.networkdiscovery.service.SpringServiceImpl;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Attribute;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.DataQuality;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.jknack.handlebars.internal.Files;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = { "serviceDecomposition.host=localhost", "serviceDecomposition.port=3333",
        "networkDiscoveryMicroService.host=localhost", "networkDiscoveryMicroService.port=9808",
        "networkDiscoveryMicroService.responseTimeOutInMilliseconds=1000" })
public class NetworkDiscoveryContextBuilderTest {

    private String authorization = "Basic "
            + Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes(StandardCharsets.UTF_8));
    private String partnerName = "POMBA";
    private String transactionId = UUID.randomUUID().toString();
    private String serviceInstanceId = "c6456519-6acf-4adb-997c-3c363dd4caaf";
    private String requestId = "2131__1";
    private String resourceType = "vserver";
    private String resourceId = "25fb07ab-0478-465e-a021-6384ac299671";

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    NetworkDiscoveryNotification networkDiscoveryNotification = simulateNetworkDiscoveryNotification();

    @Autowired
    Environment environment;

    @Autowired
    RestService restService;

    @Rule
    public WireMockRule serviceDecompositionRule = new WireMockRule(wireMockConfig().port(3333));
    @Rule
    public WireMockRule networkDiscoveryMicroServiceRule = new WireMockRule(wireMockConfig().port(9808));

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testVerifyNoAuthoriztion() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, null, partnerName, transactionId,
                serviceInstanceId, null, null);
        assertTrue(response.getEntity().toString().contains("Missing Authorization: "));
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyBadAuthoriztion() throws Exception {
        String authorization = "Basic "
                + Base64.getEncoder().encodeToString(("Test" + ":" + "Fake").getBytes(StandardCharsets.UTF_8));
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                serviceInstanceId, null, null);
        assertEquals("Authorization Failed", response.getEntity().toString());
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyPartnerName() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, authorization, null, transactionId,
                serviceInstanceId, null, null);
        assertTrue(response.getEntity().toString().contains("X-ONAP-PartnerName"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyServiceInstanceId() throws Exception {
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                null, null, null);
        assertTrue(response.getEntity().toString().contains("serviceInstanceId"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyServiceDecomposition() throws Exception {

        String urlStr = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;

        File file = new File(ClassLoader.getSystemResource("SD_response.json").getFile());
        String sdResonse = Files.read(file);

        this.serviceDecompositionRule.stubFor(get(urlStr).willReturn(okJson(sdResonse)));
        addResponse_any("junit/networkDiscoveryResponse-1.json", networkDiscoveryMicroServiceRule);
        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                serviceInstanceId, null, null);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testVerifyGetContext() throws Exception {

        String serviceDecompUrl = "/service-decomposition/service/context?serviceInstanceId=" + serviceInstanceId;
        addResponse(serviceDecompUrl, "junit/serviceDecomposition-1.json", serviceDecompositionRule);
        addResponse_any("junit/networkDiscoveryResponse-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.getContext(httpServletRequest, authorization, partnerName, transactionId,
                serviceInstanceId, null, null);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUnauthorizedNetworkDiscoveryNotfi() throws Exception {
        String networkDiscoveryCallBackUrl = "/network-discovery/service/networkDiscoveryNotification";
        addResponse(networkDiscoveryCallBackUrl, "junit/networkDiscovery-1.json", networkDiscoveryMicroServiceRule);

        String badAuthorization = "Basic "
                + Base64.getEncoder().encodeToString(("Test" + ":" + "Fake").getBytes(StandardCharsets.UTF_8));
        Response response = this.restService.networkDiscoveryNotification(networkDiscoveryNotification,
                badAuthorization);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testNetworkDiscoveryNotfi() throws Exception {
        NetworkDiscoveryInfoAccess networkDiscoveryInfoAccess = new NetworkDiscoveryInfoAccess();
        simulateNetworkDiscoveryInfoList();
        String networkDiscoveryCallBackUrl = "/network-discovery/service/networkDiscoveryNotification";
        addResponse(networkDiscoveryCallBackUrl, "junit/networkDiscovery-1.json", networkDiscoveryMicroServiceRule);

        Response response = this.restService.networkDiscoveryNotification(networkDiscoveryNotification, authorization);
        NetworkDiscoveryRspInfo rsp = networkDiscoveryInfoAccess.getList(requestId);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals(rsp.getNetworkDiscoveryNotificationList().size(), 1);
    }

    private void addResponse(String path, String classpathResource, WireMockRule thisMock) throws IOException {
        String payload = readFully(ClassLoader.getSystemResourceAsStream(classpathResource));
        thisMock.stubFor(get(path).willReturn(okJson(payload)));
    }

    private void addResponse_any(String classpathResource, WireMockRule thisMock) throws IOException {
        String payload = readFully(ClassLoader.getSystemResourceAsStream(classpathResource));
        UrlPattern tPath = WireMock.anyUrl();
        thisMock.stubFor(get(tPath).willReturn(okJson(payload)));
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

    private NetworkDiscoveryNotification simulateNetworkDiscoveryNotification() {
        NetworkDiscoveryNotification notification = new NetworkDiscoveryNotification();
        Resource myResource = new Resource();
        myResource.setId("25fb07ab-0478-465e-a021-6384ac299671");
        myResource.setType("vserver");
        DataQuality dataQuality = new DataQuality();
        dataQuality.setStatus(DataQuality.Status.ok);
        myResource.setDataQuality(dataQuality);
        List<Attribute> attributeList = new ArrayList<>();
        ;
        Attribute attribute = new Attribute();
        attribute.setName("vserver-id");
        attribute.setValue("25fb07ab-0478-465e-a021-6384ac299671");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("power-state");
        attribute.setValue("1");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("vm-state");
        attribute.setValue("active");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("status");
        attribute.setValue("ACTIVE");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("host-status");
        attribute.setValue("UNKNOWN");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("updated");
        attribute.setValue("2017-11-20T04:26:13Z");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("disk-allocation-gb");
        attribute.setValue(".010");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("memory-usage-mb");
        attribute.setValue("null");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName("cpu-util-percent");
        attribute.setValue(".048");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);

        attribute.setName(".048");
        attribute.setValue("2018-07-26 01:37:07 +0000");
        attribute.setDataQuality(dataQuality);
        attributeList.add(attribute);
        myResource.setAttributeList(attributeList);

        notification.setResources(Arrays.asList(myResource));
        notification.setAckFinalIndicator(true);
        notification.setCode(200);
        notification.setRequestId(requestId);
        notification.setMessage("OK");

        return notification;
    }

    private void simulateNetworkDiscoveryInfoList() {
        NetworkDiscoveryInfoAccess networkDiscoveryInfoAccess = new NetworkDiscoveryInfoAccess();

        String requestId2 = "2131__2";
        List<String> related_request_list = new ArrayList<>();
        related_request_list.add(requestId);
        related_request_list.add(requestId2);

        NetworkDiscoveryRspInfo notif1 = new NetworkDiscoveryRspInfo();
        notif1.setRequestId(requestId);
        notif1.setResourceType(resourceType);
        notif1.setResourceId(resourceId);
        notif1.setRelatedRequestIdList(related_request_list);
        networkDiscoveryInfoAccess.updateList(requestId, notif1);

        NetworkDiscoveryRspInfo notif2 = new NetworkDiscoveryRspInfo();
        notif2.setRequestId(requestId2);
        notif2.setResourceType(resourceType);
        notif2.setResourceId(resourceId);
        notif2.setRelatedRequestIdList(related_request_list);
        networkDiscoveryInfoAccess.updateList(requestId2, notif2);
    }

    private class NetworkDiscoveryInfoAccess extends SpringServiceImpl {
        public void updateList(String requestId, NetworkDiscoveryRspInfo resp) {
            super.updateNetworkDiscoveryInfoList(requestId, resp);
        }

        public NetworkDiscoveryRspInfo getList(String requestId) {
            return super.getNetworkDiscoveryInfoList(requestId);
        }
    }
}
