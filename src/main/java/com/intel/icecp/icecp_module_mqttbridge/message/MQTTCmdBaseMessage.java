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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.intel.icecp.core.Message;
import com.intel.icecp.icecp_module_mqttbridge.MQTTBridge_Module;
import com.intel.icecp.rpc.Command;
import com.intel.icecp.rpc.OnCommandMessage;

import java.lang.reflect.Method;


@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@cmd")
@JsonInclude(value=Include.NON_NULL)
public abstract class MQTTCmdBaseMessage implements Message, OnCommandMessage<MQTTBridge_Module, Object> {
    public static final String COMMAND_CHANNEL_NAME = "/MQTTBridge-CMD";

    //returned from setup command, used by all connect commands
    public String connectionID;


    public Command toCommand(Class commandMessage) throws NoSuchMethodException {
        String methodName = "onCommandMessage";
        Method method = commandMessage.getMethod(methodName, MQTTBridge_Module.class);
        return new Command(this, method);
    }
}
