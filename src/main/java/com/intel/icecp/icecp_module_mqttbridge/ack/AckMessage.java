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

package com.intel.icecp.icecp_module_mqttbridge.ack;

import com.intel.icecp.core.Message;

/**
 * Ack Message sent on MQTT Module's outgoing acknowledgement channel on successful upload.
 *
 */
public class AckMessage implements Message {
    private final String uri;
    private final long id;

    /**
     * @param uri the URI for the channel of the persisted message
     * @param id the ID of the persisted message
     */
    public AckMessage(String uri, long id) {
        this.uri = uri;
        this.id = id;
    }

    /**
     * For serialization only; instantiates a message with null values
     */
    public AckMessage() {
        this.uri = null;
        this.id = -1;
    }

    /**
     * Get the URI
     *
     * @return the URI of the channel a message was received on
     */
    public String getUri() {
        return uri;
    }

    /**
     * Get the Id
     *
     * @return the message Id
     */
    public long getId() {
        return id;
    }
}