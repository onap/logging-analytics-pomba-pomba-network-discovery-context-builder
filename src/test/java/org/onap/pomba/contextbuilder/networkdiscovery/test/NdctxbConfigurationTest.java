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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.onap.pomba.contextbuilder.networkdiscovery.JerseyConfiguration;
import org.onap.pomba.contextbuilder.networkdiscovery.NdctxbConfiguration;
import javax.ws.rs.client.Client;

public class NdctxbConfigurationTest {

 NdctxbConfiguration configuration = mock(NdctxbConfiguration.class);

 @Test
 public void testGetUrl() {
  String url = "www.google.com";
  when(configuration.getURL()).thenReturn(url);
  assertEquals(url, configuration.getURL());
 }

 @Test
 public void testGetNdBasicAuth() {
  String msg = "Basic YWRtaW46YWRtaW4=";
  when(configuration.getNdBasicAuth()).thenReturn(msg);
  assertEquals(msg, configuration.getNdBasicAuth());
 }

 @Test
 public void testGetSdBasicAuth() {
  String msg = "Basic YWRtaW46YWRtaW4=";
  when(configuration.getSdBasicAuth()).thenReturn(msg);
  assertEquals(msg, configuration.getSdBasicAuth());
 }

 //
 @Test
 public void testGetNetworkDiscoveryURL() {
  String url = "www.amdocs.com";
  when(configuration.getNetworkDiscoveryURL()).thenReturn(url);
  assertEquals(url, configuration.getNetworkDiscoveryURL());
 }

 @Test
 public void testGetNdResponseTimeOutInMilliseconds() {
  long myTime = 2000;
  when(configuration.getNdResponseTimeOutInMilliseconds()).thenReturn(myTime);
  assertEquals(myTime, configuration.getNdResponseTimeOutInMilliseconds());
 }

 @Test
 public void testGetNetworkDiscoveryCtxBuilderPort() {
  String myPort = "9800";
  when(configuration.getNetworkDiscoveryCtxBuilderPort()).thenReturn(myPort);
  assertEquals(myPort, configuration.getNetworkDiscoveryCtxBuilderPort());
 }

}
