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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value="NdResourcesList")
public class NdResourcesList {

    @Expose
    @SerializedName("ndResourcesList")
    private List<NdResources> ndResourcesList = new ArrayList<>();

    @ApiModelProperty(value = "List of NdResources associated with the service instance")
    public List<NdResources> getNdResources() {
        return ndResourcesList;
    }
    public void setNdResources(List<NdResources> ndResourcesList) {
        this.ndResourcesList = ndResourcesList;
    }
    public void addNdResource(NdResources ndResources) {
        this.ndResourcesList.add(ndResources);
    }
}
