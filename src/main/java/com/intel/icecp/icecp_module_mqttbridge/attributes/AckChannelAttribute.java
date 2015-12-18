/*
 * ******************************************************************************
 *
 * INTEL CONFIDENTIAL
 *
 * Copyright 2013 - 2016 Intel Corporation All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Intel Corporation or its suppliers
 * or licensors. Title to the Material remains with Intel Corporation or its
 * suppliers and licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and licensors. The
 * Material is protected by worldwide copyright and trade secret laws and treaty
 * provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed
 * in any way without Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be
 * express and approved by Intel in writing.
 *
 * Unless otherwise agreed by Intel in writing, you may not remove or alter this
 * notice or any other notice embedded in Materials by Intel or Intel's
 * suppliers or licensors in any way.
 *
 * ******************************************************************************
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
