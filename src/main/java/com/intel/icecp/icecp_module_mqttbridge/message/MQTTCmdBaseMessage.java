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
