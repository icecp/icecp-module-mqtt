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
