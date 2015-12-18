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
