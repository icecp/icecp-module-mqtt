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

