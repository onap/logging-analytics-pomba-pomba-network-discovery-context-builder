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

package org.onap.pomba.contextbuilder.networkdiscovery.test.jolt;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.google.gson.Gson;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.pomba.common.datatypes.ModelContext;
import org.onap.pomba.common.datatypes.Network;
import org.onap.pomba.common.datatypes.VM;
import org.onap.pomba.contextbuilder.networkdiscovery.model.NdResources;
import org.onap.pomba.contextbuilder.networkdiscovery.util.TransformationUtil;

public class TransformationTest {

    private static final String CONFIG_JOLT_DIRECTORY = "config/jolt/";
    private static final String TEST_RESOURCES = "src/test/resources/jolt/";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testTransformNdResources() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "/serviceDecompResponse.json");
        Object sourceObject1 = JsonUtils.jsonToObject(JsonUtils.toJsonString(sourceObject));

        List<Object> chainrSpecJson = JsonUtils.filepathToList(CONFIG_JOLT_DIRECTORY + "sdToNdResourcesSpec.json");
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);
        Object output = chainr.transform(sourceObject1);

        String resultJson = JsonUtils.toJsonString(output);

        // read the result into the NqQuery class:
        Gson gson = new Gson();
        NdResources ndQuery = gson.fromJson(resultJson, NdResources.class);

        // convert ndQuery back to json:
        String ndQueryToJson = gson.toJson(ndQuery);

        // Compare with expected output:
        Object expectedObject = JsonUtils.filepathToObject(TEST_RESOURCES + "serviceDecompToNdResources-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }
    
    @Test
    public void testTransformNdResourcesL3Network() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "/serviceDecompResponse2.json");
        Object sourceObject1 = JsonUtils.jsonToObject(JsonUtils.toJsonString(sourceObject));

        List<Object> chainrSpecJson = JsonUtils.filepathToList(CONFIG_JOLT_DIRECTORY + "sdToNdResourcesSpec.json");
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);
        Object output = chainr.transform(sourceObject1);

        String resultJson = JsonUtils.toJsonString(output);

        // read the result into the NqQuery class:
        Gson gson = new Gson();
        NdResources ndQuery = gson.fromJson(resultJson, NdResources.class);

        // convert ndQuery back to json:
        String ndQueryToJson = gson.toJson(ndQuery);

        // Compare with expected output:
        Object expectedObject = JsonUtils
                .filepathToObject(TEST_RESOURCES + "serviceDecompToNdResources2-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }

    @Test
    public void testTransformModelContext() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "serviceDecompResponse.json");
        Object sourceObject1 = JsonUtils.jsonToObject(JsonUtils.toJsonString(sourceObject));

        List<Object> chainrSpecJson = JsonUtils.filepathToList(CONFIG_JOLT_DIRECTORY + "sdToModelContextSpec.json");
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);
        Object output = chainr.transform(sourceObject1);

        String resultJson = JsonUtils.toJsonString(output);

        // read the result into the NqQuery class:
        Gson gson = new Gson();
        ModelContext ndQuery = gson.fromJson(resultJson, ModelContext.class);

        // convert ndQuery back to json:
        String ndQueryToJson = gson.toJson(ndQuery);

        // Compare with expected output:
        Object expectedObject = JsonUtils
                .filepathToObject(TEST_RESOURCES + "serviceDecompToModelContext-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }

    @Test
    public void testTransformModelContextL3Network() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "serviceDecompResponse2.json");
        Object sourceObject1 = JsonUtils.jsonToObject(JsonUtils.toJsonString(sourceObject));

        List<Object> chainrSpecJson = JsonUtils.filepathToList(CONFIG_JOLT_DIRECTORY + "sdToModelContextSpec.json");
        Chainr chainr = Chainr.fromSpec(chainrSpecJson);
        Object output = chainr.transform(sourceObject1);

        String resultJson = JsonUtils.toJsonString(output);
        
        System.err.println(resultJson);

        // read the result into the NqQuery class:
        Gson gson = new Gson();
        ModelContext ndQuery = gson.fromJson(resultJson, ModelContext.class);

        // convert ndQuery back to json:
        String ndQueryToJson = gson.toJson(ndQuery);

        // Compare with expected output:
        Object expectedObject = JsonUtils
                .filepathToObject(TEST_RESOURCES + "serviceDecompToModelContext2-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }

    @Test
    public void testTransformVserverToVm() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "networkDiscoveryVserverResponse.json");

        String resultJson = TransformationUtil.transform(JsonUtils.toJsonString(sourceObject), "vserver");

        // read the result into the VM class:
        Gson gson = new Gson();
        VM ndVm = gson.fromJson(resultJson, VM.class);

        // convert VM object back to json:
        String ndQueryToJson = gson.toJson(ndVm);

        // Compare with expected output:
        Object expectedObject = JsonUtils
                .filepathToObject(TEST_RESOURCES + "networkDiscoveryVserverToVm-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }

    @Test
    public void testTransformL3NetworkToNetworkModel() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "networkDiscoveryL3NetworkResponse.json");

        String resultJson = TransformationUtil.transform(JsonUtils.toJsonString(sourceObject), "l3-network");

        // read the result into the VM class:
        Gson gson = new Gson();
        Network ndNetwork = gson.fromJson(resultJson, Network.class);

        // convert VM object back to json:
        String ndQueryToJson = gson.toJson(ndNetwork);

        // Compare with expected output:
        Object expectedObject = JsonUtils
                .filepathToObject(TEST_RESOURCES + "networkDiscoveryL3NetworkToNetwork-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));
    }

}
