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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.pomba.contextbuilder.networkdiscovery.service.rs.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@WebAppConfiguration
@SpringBootTest
public class NetworkDiscoveryContextBuilderTest {

    private String authorization = "Basic "
                                    + Base64.getEncoder()
                                    .encodeToString(("admin" + ":" + "admin")
                                    .getBytes(StandardCharsets.UTF_8));
    private String partnerName = "POMBA";
    private String transactionId = UUID.randomUUID().toString();
    private String serviceInstanceId = "c6456519-6acf-4adb-997c-3c363dd4caaf";

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

    @Autowired
    RestService restService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testVerifyNoAuthoriztion() throws Exception {
        Response response = this
                            .restService
                            .getContext(httpServletRequest,
                                        null,
                                        partnerName,
                                        transactionId,
                                        serviceInstanceId,
                                        null,
                                        null);
        assertTrue(response.getEntity().toString().contains("Missing Authorization: "));
        assertTrue(response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testVerifyBadAuthoriztion() throws Exception {
        String authorization = "Basic "
                + Base64.getEncoder()
               .encodeToString(("Test" + ":" + "Fake")
               .getBytes(StandardCharsets.UTF_8));
        Response response = this
                            .restService
                            .getContext(httpServletRequest,
                                        authorization,
                                        partnerName,
                                        transactionId,
                                        serviceInstanceId,
                                        null,
                                        null);
        assertEquals(response.getEntity().toString(), "Authorization Failed");
        assertTrue(response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    @Test
    public void testVerifyPartnerName() throws Exception {
        Response response = this
                            .restService
                            .getContext(httpServletRequest,
                                        authorization,
                                        null,
                                        transactionId,
                                        serviceInstanceId,
                                        null,
                                        null);
        assertTrue(response.getEntity().toString().contains("X-ONAP-PartnerName"));
        assertTrue(response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testServiceInstanceId() throws Exception {
        Response response = this
                            .restService
                            .getContext(httpServletRequest,
                                        authorization,
                                        partnerName,
                                        transactionId,
                                        null,
                                        null,
                                        null);
        assertTrue(response.getEntity().toString().contains("serviceInstanceId"));
        assertTrue(response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

}
