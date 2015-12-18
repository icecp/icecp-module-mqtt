/**
 * Configure and Connect script for MQTT Bridge INBOUND
 * This script configures the MQTT Bridge module for a broker
 * and connects to the broker for bridging inbound messages
 * from a MQTT Broker to a Channel.
 * 
 * Innvocation:
 * To run this script use the Nashorn jjs script engine.
 * CD to the icecp-node folder
 * jjs -cp "..\icecp-module-mqttbridge\target\icecp-module-mqttbridge-0.0.1-jar-with-dependencies.jar" ..\icecp-module-mqttbridge\scripts\Inbound_1_SetupForTest.js
 * 
 * Parameters required for a broker, setup below in the code:
 * brokerURL	The URL to the MQTT Broker.  eg: tcp://myhost.com
 * clientId		The unique id for the client.  This id can NOT match the id used by the MQTTBridge module.
 * topic		The MQTT topic to subscribe to
 * userName		The user name if needed, else empty string
 * passWord		The password if needed, else emptyp string
 * 
 * Parameters for ICECP
 * mqttCmdChannelName - the MqttBridgeModule command channel name
 * mqttCmdReturnChannelName - The return channel for the MqttBridgeModule
 * bridgeChannelName - the ICECP channel to forward the incoming MqttBroker messages to
 * 
 */
//Local MQTT Broker (mosquitto)
var brokerURL = "tcp://" + java.net.InetAddress.getLocalHost().getHostAddress() + ":1883";
var clientId = "ClientID2";
var topic = "myTopic";
var userName = "";
var passWord = "";

//Cloud MQTT Broker (bluemix)
//var brokerURL = "ssl://cloqzw.messaging.internetofthings.ibmcloud.com";
//var clientId = "a:cloqzw:a-cloqzw-wssoma5mxv";
//var topic = "iot-2/type/TestModel/id/+/evt/+/fmt/json";  //"iot-2/cmd/+/fmt/json";
//var userName = "a-cloqzw-wssoma5mxv";
//var passWord = "hO5Nx+9kh7asqe)PAv";

var mqttCmdChannelName = "/MQTTBridge-CMD";
var mqttCmdReturnChannelName = "/MQTTBridge-CMD-Return";
var bridgeChannelName = "/MQTTBridge-Outbound";
var nodeName = "/intel/node/" + com.intel.icecp.node.utils.NetworkUtils.getHostName();
var myNode;
var mqttCmdChannel;
var mqttCmdReturnChannel;

function createNode() {
	//Create a ICECP Node and Start it
	print("configure device");
	myNode = com.intel.icecp.node.utils.StartupUtils.configureDefaultDevice(nodeName);
	
	//Setup channel names
	mqttCmdChannelName = myNode.getDefaultUri().toString() + mqttCmdChannelName;
	mqttCmdReturnChannelName = myNode.getDefaultUri().toString() + mqttCmdReturnChannelName;
	bridgeChannelName = myNode.getDefaultUri().toString() + bridgeChannelName;
	
	print("start");
	myNode.start();

	print("openChannels");
	var perst = new com.intel.icecp.core.metadata.Persistence();
	var uri = new java.net.URI(mqttCmdChannelName);
	mqttCmdChannel = myNode.openChannel(uri, com.intel.icecp.icecp_module_mqttbridge.message.MQTTCmdBaseMessage.class, perst);
	uri = new java.net.URI(mqttCmdReturnChannelName);
	mqttCmdReturnChannel = myNode.openChannel(uri, com.intel.icecp.icecp_module_mqttbridge.message.MQTTCmdBaseMessage.class, perst);
}

function configureBroker() {
	//Configure setup command
	print("create setup msg");
	var cmdInfo = new com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage();
	cmdInfo.returnChannel = mqttCmdReturnChannelName;
	cmdInfo.brokerURL = brokerURL;
	cmdInfo.clientId = clientId;
	cmdInfo.brokerUser = userName;
	cmdInfo.brokerPassword = passWord;
	//Publish the configure message to the ICECP Module
	print("publish");
	mqttCmdChannel.publish(cmdInfo);
	java.lang.Thread.sleep(1000);
}

function getResponse() {
	var returnMessage = mqttCmdReturnChannel.latest().get();
	java.lang.Thread.sleep(1000);
	return returnMessage;
}

function connectToBroker(connectionID) {
	//Configure connect command
	var cmdInfo = new com.intel.icecp.icecp_module_mqttbridge.message.MQTTConnectMessage();
	cmdInfo.returnChannel = mqttCmdReturnChannelName;
	cmdInfo.connectionID = connectionID;
	cmdInfo.direction = "inbound";
	cmdInfo.source = topic;
	cmdInfo.destination = bridgeChannelName;
	cmdInfo.start = true;
	//publish connect command to ICECP Module
	mqttCmdChannel.publish(cmdInfo);
	java.lang.Thread.sleep(1000);
}

print("STEP 1: Create Node, open channels");
createNode();
print ("STEP 2: Configure Broker...");
configureBroker();
var returnMessage = getResponse();
print ("Response=" + returnMessage.returnStatus);

print ("STEP 3: Connect to Broker: connectionID=" + returnMessage.connectionID);
connectToBroker(returnMessage.connectionID);
returnMessage = getResponse();
print ("Response=" + returnMessage.returnStatus);

print ("Connection complete");




