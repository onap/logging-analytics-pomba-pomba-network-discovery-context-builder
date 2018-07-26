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

package org.onap.pomba.contextbuilder.sd.model;

import java.util.List;

import org.onap.sdnc.apps.pomba.networkdiscovery.datamodel.NetworkDiscoveryResponse;

public class NetworkDiscoveryRspInfo {

	private String requestId;
    private String vServerId;
    private String status;
    private String serviceInstanceId;
    
    private List<NetworkDiscoveryResponse> networkDiscoveryRspList;
     
    public String getRequestId() {
		return requestId;
	}

    public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getvServerId() {
		return vServerId;
	}

	public void setvServerId(String vServerId) {
		this.vServerId = vServerId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}  
	
    public List<NetworkDiscoveryResponse> getNetworkDiscoveryRspList() {
        return this.networkDiscoveryRspList;
    }

    public void setNetworkDiscoveryRspList(List<NetworkDiscoveryResponse> response) {
        this.networkDiscoveryRspList = response;
    }
     
    @Override
    public String toString() {
        return "networkDiscoveryRspInfo [requestId=" + this.requestId + ", vServerId=" + this.vServerId + ", status=" + this.status + "]";
    }
}
