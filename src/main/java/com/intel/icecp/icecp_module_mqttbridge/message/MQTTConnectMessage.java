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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.icecp.icecp_module_mqttbridge.MQTTBridge_Module;
import com.intel.icecp.icecp_module_mqttbridge.MqttWorkerRunnable;
import com.intel.icecp.icecp_module_mqttbridge.exceptions.MQTTBridgeModuleException;

/**
 * Connect Message 
 * 
 * direction: inbound or outbound
 * source		inbound: topic for broker;	outbound: node channel
 * destination	inbound: node channel;		outbound: topic for broker
 *
 */
public class MQTTConnectMessage extends MQTTCmdBaseMessage {
    @JsonIgnore
    public static final String INBOUND = "inbound";

    @JsonIgnore
    public static final String OUTBOUND = "outbound";

    @JsonIgnore
    public static final String OUTBOUND_ACK = "outboundack";

    //For connect command
    public String direction;	//inbound or outbound
    public String source;		//inbound: topic for broker;	outbound: node channel
    public String destination;	//inbound: node channel;		outbound: topic for broker
    public boolean start;
    public boolean wrapPayload;

    @Override
    public Object onCommandMessage(MQTTBridge_Module context) throws MQTTBridgeModuleException {
        MqttWorkerRunnable worker = context.getBroker(connectionID);
        checkForValidConnection(worker);

        checkForValidDirection();

        switch (direction.toLowerCase()) {
        case INBOUND:
            worker.addInboundConnection(source, destination, wrapPayload);
            MQTTBridge_Module.logger.info("Connect INBOUND src[{}] dst[{}]", source, destination);
            break;

        case OUTBOUND:
            worker.addOutboundConnection(source, destination, wrapPayload);
            MQTTBridge_Module.logger.info("Connect OUTBOUND src[{}] dst[{}]", source, destination);
            break;

        case OUTBOUND_ACK:
            worker.addOutboundAckConnection(source, destination, wrapPayload);
            MQTTBridge_Module.logger.info("Connect OUTBOUND with Acknowledgements src[{}] dst[{}]", source, destination);
            break;

        default:
            MQTTBridge_Module.logger.error("Unknown direction, neither inbound nor outbound : " + direction);
            throw new MQTTBridgeModuleException(String.format("Unknown direction, neither inbound nor outbound : " + direction));
        }

        if (start) {
            MQTTBridge_Module.logger.info("Starting worker thread " + worker.toString());
            Thread workerThread = new Thread(worker);
            workerThread.setName(worker.toString());
            workerThread.start();
        }

        return "success";
    }

    private void checkForValidConnection(MqttWorkerRunnable worker) throws MQTTBridgeModuleException {
        if (worker == null) {
            MQTTBridge_Module.logger.error("Cannot find connection for : " + connectionID);
            throw new MQTTBridgeModuleException(String.format("Cannot find connection for : %s", connectionID));
        }
    }

    private void checkForValidDirection() throws MQTTBridgeModuleException{
        if (direction == null || direction.isEmpty())
            throw new MQTTBridgeModuleException("Required parameter is null or empty: Direction.");
    }
}
