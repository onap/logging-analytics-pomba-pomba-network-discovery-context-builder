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
package org.onap.pomba.contextbuilder.networkdiscovery.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryNotification;

public class NetworkDiscoveryRspInfo {

    private String requestId;
    private String resourceId; // e.g.: vserverId
    private String resourceType; // e.g.: vserver
    private CountDownLatch latchSignal;

    private List<NetworkDiscoveryNotification> networkDiscoveryNotificationList = Collections
            .synchronizedList(new ArrayList<>());
    private List<String> relatedRequestIdList;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public CountDownLatch getLatchSignal() {
        return latchSignal;
    }

    public void setLatchSignal(CountDownLatch latchSignal) {
        this.latchSignal = latchSignal;
    }

    public List<NetworkDiscoveryNotification> getNetworkDiscoveryNotificationList() {
        return this.networkDiscoveryNotificationList;
    }

    public void setNetworkDiscoveryNotificationList(List<NetworkDiscoveryNotification> response) {
        this.networkDiscoveryNotificationList = response;
    }

    public List<String> getRelatedRequestIdList() {
        return this.relatedRequestIdList;
    }

    public void setRelatedRequestIdList(List<String> response) {
        this.relatedRequestIdList = response;
    }

    @Override
    public String toString() {
        return "networkDiscoveryRspInfo [requestId=" + this.requestId + ", resourceId=" + this.resourceId
                + ", resourceType=" + this.resourceType + "]";
    }
}
