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
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTCloseMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTConnectMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;
import com.intel.icecp.rpc.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

/**
 */
public class CommandAdapter {
    public static final Logger logger = LogManager.getLogger(CommandAdapter.class.getName());

    private MQTTBridge_Module context;

    public CommandAdapter(MQTTBridge_Module context) {
        this.context = context;
    }

    public Object mqttSetup(Map<String, String> inputs) throws MQTTBridgeModuleException {
        MQTTSetupMessage message = new MQTTSetupMessage();

        message.brokerURL = inputs.get("brokerURL");
        message.clientId = inputs.get("clientId");
        message.brokerUser = inputs.get("brokerUser");
        message.brokerPassword = inputs.get("brokerPassword");
        message.proxyHost = inputs.get("proxyHost");

        try {
            message.proxyPort = Integer.parseInt(inputs.getOrDefault("proxyPort", "1080"));
        } catch (NumberFormatException e) {
            logger.warn("proxyPort input not integer, default to 1080");
            message.proxyPort = 1080;
        }

        return message.onCommandMessage(context);
    }

    public Object mqttConnect(Map<String, String> inputs) throws MQTTBridgeModuleException {
        MQTTConnectMessage message = new MQTTConnectMessage();

        message.direction = inputs.get("direction");
        message.source = inputs.get("source");
        message.destination = inputs.get("destination");
        message.start = "true".equals(inputs.get("start"));
        message.connectionID = inputs.get("connectionId");
        message.wrapPayload = "true".equals(inputs.get("wrapPayload"));

        return message.onCommandMessage(context);
    }

    public Object mqttClose(Map<String, String> inputs) {
        MQTTCloseMessage message = new MQTTCloseMessage();
        message.connectionID = inputs.get("connectionId");

        return message.onCommandMessage(context);
    }

    public Command toCommand(String methodName) throws NoSuchMethodException {
        Method method = getClass().getMethod(methodName, Map.class);

        return new Command(methodName, this, method);
    }
}
