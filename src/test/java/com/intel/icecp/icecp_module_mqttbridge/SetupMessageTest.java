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

import com.intel.icecp.icecp_module_mqttbridge.exceptions.MQTTBridgeModuleException;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;

/**
 */
public class SetupMessageTest {
    @Mock
    private MQTTBridge_Module mockModule;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullClientIdCreatedRandomlyGeneratedClientId() throws MQTTBridgeModuleException, UnknownHostException {
        MQTTSetupMessage message = new MQTTSetupMessage();
        message.brokerURL = "tcp://" + java.net.InetAddress.getLocalHost().getHostAddress() + ":1883";

        message.onCommandMessage(mockModule);
        assertNotNull(message.clientId);
    }

    @Test
    public void testEmptyClientIdCreatedRandomlyGeneratedClientId() throws MQTTBridgeModuleException, UnknownHostException {
        MQTTSetupMessage message = new MQTTSetupMessage();
        message.brokerURL = "tcp://" + java.net.InetAddress.getLocalHost().getHostAddress() + ":1883";
        message.clientId = "";

        message.onCommandMessage(mockModule);
        assertNotNull(message.clientId);
    }

    @Test
    public void throwOnNullBrokerUrl() throws MQTTBridgeModuleException {
        MQTTSetupMessage message = new MQTTSetupMessage();
        message.clientId = "localMqttBroker";

        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }

    @Test
    public void throwOnEmptyBrokerUrl() throws MQTTBridgeModuleException {
        MQTTSetupMessage message = new MQTTSetupMessage();
        message.brokerURL = "";
        message.clientId = "localMqttBroker";

        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }

    @Test
    public void onCommandWithASetupSuccess() throws UnknownHostException, MQTTBridgeModuleException {
        MQTTSetupMessage message = new MQTTSetupMessage();
        message.brokerURL = "tcp://" + java.net.InetAddress.getLocalHost().getHostAddress() + ":1883";
        message.clientId = "localMqttBroker";
        String connectionID = (String) message.onCommandMessage(mockModule);

        assertNotNull(connectionID);
    }
}
