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
package org.onap.pomba.contextbuilder.networkdiscovery.test;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NetworkDiscoveryRspInfo;
import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = { "enricher.url=http://localhost:9505", 
        "networkDiscoveryCtxBuilder.userId=admin",
        "networkDiscoveryCtxBuilder.password=OBF:1u2a1toa1w8v1tok1u30",
        "serviceDecomposition.host=localhost",
        "serviceDecomposition.userId=admin",
        "serviceDecomposition.password=OBF:1u2a1toa1w8v1tok1u30",
        "networkDiscoveryMicroService.userId=admin",
        "networkDiscoveryMicroService.password=OBF:1u2a1toa1w8v1tok1u30",
        "networkDiscoveryMicroService.host=localhost",
        "networkDiscoveryMicroService.responseTimeOutInMilliseconds=1000" })
public class NetworkDiscoveryRspInfoTest {
    NetworkDiscoveryRspInfo networkDiscoveryRspInfo = new NetworkDiscoveryRspInfo();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetRequestId() throws Exception {
        String requestId = "2123";
        networkDiscoveryRspInfo.setRequestId(requestId);
        assertEquals(requestId, networkDiscoveryRspInfo.getRequestId());
    }

    @Test
    public void testGetResourceType() throws Exception {
        String resourceType = "vserver";
        networkDiscoveryRspInfo.setResourceType(resourceType);
        assertEquals(resourceType, networkDiscoveryRspInfo.getResourceType());
    }

    @Test
    public void testGetResourceId() throws Exception {
        String resourceId = "2123";
        networkDiscoveryRspInfo.setResourceId(resourceId);
        assertEquals(resourceId, networkDiscoveryRspInfo.getResourceId());
    }

    @Test
    public void testGetLatchSignal() throws Exception {
        CountDownLatch latchSignal = new CountDownLatch(5);
        networkDiscoveryRspInfo.setLatchSignal(latchSignal);
        assertEquals(latchSignal, networkDiscoveryRspInfo.getLatchSignal());
    }

    @Test
    public void testGetNetworkDiscoveryNotificationList() throws Exception {
        NetworkDiscoveryNotification tmpNof = new NetworkDiscoveryNotification();
        List<NetworkDiscoveryNotification> myList = Arrays.asList(tmpNof);

        networkDiscoveryRspInfo.setNetworkDiscoveryNotificationList(myList);
        networkDiscoveryRspInfo.toString();
        assertEquals(myList, networkDiscoveryRspInfo.getNetworkDiscoveryNotificationList());
    }

    @Test
    public void testGetRelatedRequestIdList() throws Exception {
        List<String> myList = Arrays.asList("myTest123");

        networkDiscoveryRspInfo.setRelatedRequestIdList(myList);
        assertEquals(myList, networkDiscoveryRspInfo.getRelatedRequestIdList());
    }
}
