/*
 * ******************************************************************************
 *
 *  INTEL CONFIDENTIAL
 *
 *  Copyright 2016 Intel Corporation All Rights Reserved.
 *
 *  The source code contained or described herein and all documents related to the
 *  source code ("Material") are owned by Intel Corporation or its suppliers or
 *  licensors. Title to the Material remains with Intel Corporation or its
 *  suppliers and licensors. The Material contains trade secrets and proprietary
 *  and confidential information of Intel or its suppliers and licensors. The
 *  Material is protected by worldwide copyright and trade secret laws and treaty
 *  provisions. No part of the Material may be used, copied, reproduced, modified,
 *  published, uploaded, posted, transmitted, distributed, or disclosed in any way
 *  without Intel's prior express written permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual
 *  property right is granted to or conferred upon you by disclosure or delivery of
 *  the Materials, either expressly, by implication, inducement, estoppel or
 *  otherwise. Any license under such intellectual property rights must be express
 *  and approved by Intel in writing.
 *
 *  Unless otherwise agreed by Intel in writing, you may not remove or alter this
 *  notice or any other notice embedded in Materials by Intel or Intel's suppliers
 *  or licensors in any way.
 *
 * *********************************************************************
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
