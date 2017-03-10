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
package com.intel.icecp.icecp_module_mqttbridge.message;

import com.intel.icecp.icecp_module_mqttbridge.MQTTBridge_Module;
import com.intel.icecp.icecp_module_mqttbridge.exceptions.MQTTBridgeModuleException;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class MQTTSetupMessage extends MQTTCmdBaseMessage {

    //For setup command
    public String brokerURL;
    public String clientId;
    public String brokerUser;
    public String brokerPassword;
    public String proxyHost;
    public int proxyPort;

    @Override
    public Object onCommandMessage(MQTTBridge_Module context) throws MQTTBridgeModuleException {
        if (brokerURL == null || brokerURL.isEmpty())
            throw new MQTTBridgeModuleException("Required parameter is null or empty: BrokerURL.");
        if (clientId == null || clientId.isEmpty()) {
            MQTTBridge_Module.logger.info("clientId is null/empty, auto-generating a unique clientId for the connection");
            clientId = MqttClient.generateClientId();
        }

        connectionID = String.format("%s_%s_%d", brokerURL, clientId, System.currentTimeMillis());

        context.putBroker(this);
        MQTTBridge_Module.logger.info(String.format("Return: ConnectionID[%s]", connectionID));

        return connectionID;
    }
}
