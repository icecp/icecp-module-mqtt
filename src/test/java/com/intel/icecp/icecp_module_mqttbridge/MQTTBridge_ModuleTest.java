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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.net.URI;

import com.intel.icecp.core.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Message;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.IdAttribute;
import com.intel.icecp.core.management.Channels;
import com.intel.icecp.core.metadata.Persistence;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.misc.Configuration;
import com.intel.icecp.icecp_module_mqttbridge.exceptions.MQTTBridgeModuleException;


public class MQTTBridge_ModuleTest  {

    @Mock
    private Node mockNode;
    @Mock
    private Channels mockChannels;
    @Mock
    private Configuration mockCfg;
    @Mock
    private Channel<Module.State> mockStatusChannel;
    @Mock
    private Channel<Message> mockResponseChannel;
    @Mock
    private Attributes mockAttributes;
    private static final long MODULE_ID = 12;

    MQTTBridge_Module module = new MQTTBridge_Module();

    @Before
    public void beforeTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockNode.channels()).thenReturn(mockChannels);
        when(mockNode.channels().openChannel(any(URI.class), (Class<Message>)any(), any(Persistence.class))).thenReturn(mockResponseChannel);
        when(mockAttributes.get(eq(IdAttribute.class))).thenReturn(MODULE_ID);
    }

    @After
    public void afterTest() throws Exception {
    }

    @Test
    public void addCommandsToRegistry() throws NoSuchMethodException, ChannelIOException, MQTTBridgeModuleException, ChannelLifetimeException {
        module.run(mockNode, mockAttributes);
        assertEquals(module.getRpcServer().registry().size(), 3);
    }
}

