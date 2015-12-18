/**
 * Read from brige channel
 * This script will read the latest message on the Bridge channel.
 * Specify the bridgeChannelName and nodeName
 */
var bridgeChannelName = "/MQTTBridge-Outbound";
var nodeName = "/intel/node/" + com.intel.icecp.node.utils.NetworkUtils.getHostName();

function getInboundMessage() {
	var myNode = com.intel.icecp.node.utils.StartupUtils.configureDefaultDevice(nodeName);
	
	bridgeChannelName = myNode.getDefaultUri().toString() + bridgeChannelName;
	
	myNode.start();
	var uri = new java.net.URI(bridgeChannelName);
	var perst = new com.intel.icecp.core.metadata.Persistence();
	var channel = myNode.openChannel(uri, com.intel.icecp.icecp_module_mqttbridge.message.MQTTMessage.class, perst);
	var returnMQTTMessage = channel.latest().get();
	java.lang.Thread.sleep(1000);
	channel.close();
	return returnMQTTMessage.toString();
}

print("Get latest Message");
var message = getInboundMessage();
print("Message: " + message);

