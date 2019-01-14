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

@ApiModel(value="NdQuery")
public class NdQuery {

        @Expose
        @SerializedName("ndQuery")
        private List<NdResourcesList> ndQuery = new ArrayList<>();

        @ApiModelProperty(value = "List of NdResource associated with the service instance")
        public List<NdResourcesList> getNdQuery() {
            return ndQuery;
        }
        public void setNdQuery(List<NdResourcesList> ndQuery) {
            this.ndQuery = ndQuery;
        }
        public void addNdQuery(NdResourcesList ndResourcesList) {
            this.ndQuery.add(ndResourcesList);
        }
}
