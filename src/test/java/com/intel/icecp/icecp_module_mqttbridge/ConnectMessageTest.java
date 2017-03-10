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
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTConnectMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Natalie Gaston, natalie.gaston@intel.com on 6/6/2016.
 */
public class ConnectMessageTest {
    @Mock
    private MQTTBridge_Module mockModule;
    @Mock
    private MQTTSetupMessage mockSetupMessage;
    @Mock
    private MqttWorkerRunnable mockWorker;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    MQTTConnectMessage message = new MQTTConnectMessage();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        message.direction = "inbound";
        message.source = "/sensor/SunsetPassTAP";
        message.destination = "$node/sunset-pass-tap-unfiltered";
        message.connectionID = "mockConnectionId";
        when(mockModule.getBroker("mockConnectionId")).thenReturn(mockWorker);
    }

    @Test
    public void succeedWithValidInputs() throws Exception {
        assertEquals("success", message.onCommandMessage(mockModule));
    }

    @Test
    public void throwOnNullDirection() throws Exception {
        message.direction = null;
        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }

    @Test
    public void throwOnEmptyDirection() throws Exception {
        message.direction = "";
        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }

    @Test
    public void throwOnInvalidConnectionId() throws MQTTBridgeModuleException {
        message.connectionID = "";
        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }

    @Test
    public void throwOnInvalidDirection() throws MQTTBridgeModuleException {
        message.direction = "nobound";

        exception.expect(MQTTBridgeModuleException.class);
        message.onCommandMessage(mockModule);
    }
}
