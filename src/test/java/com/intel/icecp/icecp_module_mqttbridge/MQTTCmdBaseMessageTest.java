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

import static org.junit.Assert.*;

import com.intel.icecp.icecp_module_mqttbridge.message.MQTTCloseMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTCmdBaseMessage;
import com.intel.icecp.rpc.Command;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.icecp.core.Message;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;


public class MQTTCmdBaseMessageTest  {

	ObjectMapper mapper = new ObjectMapper();  
	
	@Before
	public void beforeTest() throws Exception {
		
	}
	
	@After
	public void AfterTest() throws Exception {
	}
	
	/*
	 * test proper serialization and de-serialization
	 * ensure @JsonTypeInfo is properly applied
	 * 
	 * @JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@cmd")   
	 */
	@Test
	public void serializeCorrectlyTest () throws Exception {
		Message msg = new MQTTSetupMessage();
		String s = mapper.writeValueAsString(msg);
		assertTrue (s.contains("@cmd"));
		assertTrue (s.contains(".MQTTSetupMessage"));
		Message m2 = mapper.readValue(s, MQTTCmdBaseMessage.class);
		assertTrue (m2 instanceof MQTTSetupMessage);
	}
	
	@Test
	public void createCorrectCommandTest() throws NoSuchMethodException {
		Command command = (new MQTTCloseMessage()).toCommand(MQTTCloseMessage.class);
		String name = command.name();
		assertEquals("MQTTCloseMessage.onCommandMessage", name);
	}
}
