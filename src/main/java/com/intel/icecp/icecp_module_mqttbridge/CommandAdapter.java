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
