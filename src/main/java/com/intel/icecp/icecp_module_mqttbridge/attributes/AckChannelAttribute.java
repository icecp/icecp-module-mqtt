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

package com.intel.icecp.icecp_module_mqttbridge.attributes;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.intel.icecp.core.attributes.BaseAttribute;

/**
 * Channel on which MQTT Module sends acknowledgement on successful upload.
 * 
 */
public class AckChannelAttribute extends BaseAttribute<URI> {

    public static final String MQTT_ACK_CHANNEL = "outgoing-ack-channel";
    private static final Logger LOGGER = LogManager.getLogger();
    private URI ackChannelURI;

    /**
     * Constructor to create the outgoing-ack-channel attribute.
     *
     * @param uri value of the attribute
     */
    public AckChannelAttribute(String uri) {
        super(MQTT_ACK_CHANNEL, URI.class);
        if (uri == null) {
            ackChannelURI = null;
            return;
        }

        try {
            ackChannelURI = new URI(uri);
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid URI received for Tap Inbound Channel attribute<" + uri + ">", e);
            ackChannelURI = null;
        }
        assert ackChannelURI != null;
    }

    /**
     * Constructor
     *
     * @param uri the uri of the inbound tap channel
     */
    public AckChannelAttribute(URI uri) {
        super(MQTT_ACK_CHANNEL, URI.class);
        this.ackChannelURI = uri;
    }

    @Override
    public URI value() {
        return ackChannelURI;
    }

}
