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
