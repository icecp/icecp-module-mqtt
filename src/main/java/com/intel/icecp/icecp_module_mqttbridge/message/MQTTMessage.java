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

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.intel.icecp.core.Message;

public class MQTTMessage extends MqttMessage implements Message {

    public MQTTMessage(String initialPayload) {
        super(initialPayload.getBytes());
    }

    public MQTTMessage() {
        super();
    }

    public MQTTMessage(MqttMessage msg) {
        super();
        super.setPayload(msg.getPayload());
        super.setDuplicate(msg.isDuplicate());
        super.setQos(msg.getQos());
        super.setRetained(msg.isRetained());
    }

    public MqttMessage fromMQTTMessage() {
        MqttMessage msg = new MqttMessage();
        msg.setPayload(this.getPayload());
        msg.setQos(this.getQos());
        msg.setRetained(this.isRetained());
        return msg;
    }
}
