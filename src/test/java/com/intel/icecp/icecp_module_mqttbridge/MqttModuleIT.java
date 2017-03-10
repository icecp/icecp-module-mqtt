/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.icecp.icecp_module_mqttbridge;

import com.intel.icecp.core.Module.StopReason;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.AttributeRegistrationException;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.IdAttribute;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.core.management.Channels;
import com.intel.icecp.icecp_module_mqttbridge.attributes.AckChannelAttribute;
import com.intel.icecp.node.AttributesFactory;
import com.intel.icecp.node.NodeFactory;
import com.intel.icecp.rpc.CommandRequest;
import com.intel.icecp.rpc.CommandResponse;
import com.intel.icecp.rpc.Rpc;
import com.intel.icecp.rpc.RpcClient;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 */
public class MqttModuleIT {
    private static final URI MQTT_MODULE_URI = URI.create("mock:/mqtt/module");
    private static final String BROKER_URI = "tcp://localhost:1883";
    private static final String CLIENT_ID = "test";
    private static final String BROKER_USER = "test";
    private static final String BROKER_PASSWORD = "test";
    private static final int moduleId = 47;

    private Node node;
    private MQTTBridge_Module mqttModule;
    private RpcClient rpcClient;
    private String connectionId;

    @Before
    public void beginMqttModule() throws Exception {
        node = NodeFactory.buildMockNode();

        mqttModule = new MQTTBridge_Module();
        mqttModule.run(node, getMqttModuleAttributes(node.channels(), MQTT_MODULE_URI));
        rpcClient = Rpc.newClient(node.channels(), new URI("ndn:" + mqttModule.getName() + "/MQTTBridge-CMD/" + moduleId));

        connectionId = createBroker(CLIENT_ID, BROKER_URI);
    }

    @Test
    public void setupMqttBroker() throws Exception {
        assertEquals(1, mqttModule.getAllBrokers().size());
    }

    @Test
    public void stopMqttBroker() throws Exception {
        assertEquals(1, mqttModule.getAllBrokers().size());
        mqttModule.stop(StopReason.NODE_SHUTDOWN);
        assertEquals(0, mqttModule.getAllBrokers().size());
    }

    @Test
    public void inboundMqttBroker() throws Exception {
        assertEquals(1, mqttModule.getAllBrokers().size());
        assertEquals("success", createConnection("inbound", connectionId, "ndn:/test/destination", "/test/source"));
        assertEquals("success", createConnection("outbound", connectionId, "/test/intel_retail_topic", "ndn:/test/sunset-pass-tap-filtered"));
    }

    private String createBroker(String clientId, String brokerUri) {
        Map<String, String> commandMap = new HashMap<>();
        commandMap.put("clientId", clientId);
        commandMap.put("brokerURL", brokerUri);
        commandMap.put("brokerUser", BROKER_USER);
        commandMap.put("brokerPassword", BROKER_PASSWORD);
        commandMap.put("proxyHost", "");
        commandMap.put("proxyPort", "80");

        CommandResponse response;
        try {
            response = rpcClient.call((CommandRequest.from("mqttSetup", commandMap))).get();
            assertTrue(response.out.toString().matches("tcp://(.*)localhost:(.*)"));
            return response.out.toString();
        } catch (InterruptedException | ExecutionException e) {
            fail("Failed to create broker");
            return "";
        }
    }

    private String createConnection(String direction, String connectionId, String destination, String source) {
        Map<String, String> commandMap = new HashMap<>();
        commandMap.put("direction", direction);
        commandMap.put("destination", destination);
        commandMap.put("source", source);
        commandMap.put("connectionId", connectionId);
        commandMap.put("start", "true");

        CommandResponse response;
        try {
            response = rpcClient.call((CommandRequest.from("mqttConnect", commandMap))).get();
            assertTrue(response.out.toString().matches("success"));
            return response.out.toString();
        } catch (InterruptedException | ExecutionException e) {
            fail("Failed to create connection");
            return "";
        }
    }

    private Attributes getMqttModuleAttributes(Channels channels, URI mqttModuleUri) throws AttributeRegistrationException, URISyntaxException {
        Attributes attributes = AttributesFactory.buildEmptyAttributes(channels, mqttModuleUri);
        attributes.add(new IdAttribute(moduleId));
        attributes.add(new ModuleStateAttribute());
        attributes.add(new AckChannelAttribute("ndn:/intel/mqtt/ack"));
        return attributes;
    }
}