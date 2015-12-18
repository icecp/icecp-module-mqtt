/**
 * Send a MQTT Message over a channel
 * The channel is bridged to a MQTT Broker
 * Specify the node name, bridge channel, and messages 
 */

var bridgeChannelName = "/MQTTBridge-Outbound";
var nodeName = "/intel/node/" + com.intel.icecp.node.utils.NetworkUtils.getHostName();
var message = "Sending this message";

function sendMessage() {
	var myNode = com.intel.icecp.node.utils.StartupUtils.configureDefaultDevice(nodeName);
	bridgeChannelName = myNode.getDefaultUri().toString() + bridgeChannelName;
	myNode.start();
	var uri = new java.net.URI(bridgeChannelName);
	var perst = new com.intel.icecp.core.metadata.Persistence();
	var channel = myNode.openChannel(uri, com.intel.icecp.core.messages.BytesMessage.class, perst);
	
	channel.publish(new com.intel.icecp.core.messages.BytesMessage(message.getBytes()));
	java.lang.Thread.sleep(1000);
	channel.close();
}

print("Send Message to Broker");
sendMessage();
