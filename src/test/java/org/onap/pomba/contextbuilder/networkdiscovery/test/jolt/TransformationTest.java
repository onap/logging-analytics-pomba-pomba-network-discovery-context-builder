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
import org.onap.pomba.contextbuilder.networkdiscovery.model.NdQuery;

public class TransformationTest {

    private static final String TEST_RESOURCES = "src/test/resources/jolt/";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    @Test
    public void testTransformNdQuery() {

        Object sourceObject = JsonUtils.filepathToObject(TEST_RESOURCES + "serviceDecompToNdQuery-input.json");
        Object sourceObject1 = JsonUtils.jsonToObject(JsonUtils.toJsonString(sourceObject));
        
        List<Object> chainrSpecJSON = JsonUtils.filepathToList("config/ndQuery.spec");
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
        Object output = chainr.transform(sourceObject1);

        String resultJson = JsonUtils.toJsonString(output);
 
        // read the result into the NqQuery class:
        Gson gson = new Gson();
        NdQuery ndQuery = gson.fromJson(resultJson, NdQuery.class);
        
        // convert ndQuery back to json:
        String ndQueryToJson = gson.toJson(ndQuery);

        // Compare with expected output:
        Object expectedObject = JsonUtils.filepathToObject(TEST_RESOURCES + "serviceDecompToNdQuery-expected.json");

        Assert.assertEquals("Json transformation result does not match expected content",
                JsonUtils.toPrettyJsonString(expectedObject),
                JsonUtils.toPrettyJsonString(JsonUtils.jsonToObject(ndQueryToJson)));

    }

}
