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