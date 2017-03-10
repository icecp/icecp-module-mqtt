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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Module;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.AttributeNotFoundException;
import com.intel.icecp.core.attributes.AttributeNotWriteableException;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.IdAttribute;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.misc.Configuration;
import com.intel.icecp.core.modules.ModuleProperty;
import com.intel.icecp.icecp_module_mqttbridge.attributes.AckChannelAttribute;
import com.intel.icecp.icecp_module_mqttbridge.exceptions.MQTTBridgeModuleException;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTCloseMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTCmdBaseMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTConnectMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;
import com.intel.icecp.node.utils.ChannelUtils;
import com.intel.icecp.rpc.Rpc;
import com.intel.icecp.rpc.RpcServer;

/**
 * MQTT Bridge module for ICECP
 * <p>
 * This module is a bridge for mqtt messages. It will forward INBOUND messages from an mqtt topic to a channel, and/or
 * forward OUTBOUND messages from a channel to an mqtt topic. Which mqtt broker to connect to, and the connection points
 * are all setup by sending commands to the module. The module listens for commands on its well known command channel:
 * {@link MQTTCmdBaseMessage#COMMAND_CHANNEL_NAME}.
 * <p>
 * The steps to setup an mqtt broker, create INBOUND/OUTBOUND bridge connections and close the connections are:
 * <p>
 * <b>Load</b>
 * <p>
 * Load this module onto a ICECP node. Build the jar file using maven and the pom file. Then start up the ICECP Node with
 * this command line argument:
 * <p>
 * -c ../icecp-module-mqttbridge/target/icecp-module-mqttbridge-0.0.1.jar
 * <p>
 * <b>Setup</b>
 * <p>
 * Open the {@link MQTTCmdBaseMessage#COMMAND_CHANNEL_NAME} channel.
 * <p>
 * Create a return channel with a unique name, and subscribe to it. This can be used for all your commands.
 * <p>
 * Create and setup an {@link MQTTSetupMessage} message.
 * <p>
 * Set the returnChannel to the channel you created above.
 * <p>
 * Set the brokerUrl and clientId for the mqtt broker to be accessed.
 * <p>
 * (optionally) Set the brokerUser and brokerPassword for accessing the mqtt broker.
 * <p>
 * (optionally) Set the proxyHost and proxyPort if a proxy is needed.
 * <p>
 * Publish the setup message on the command channel.
 * <p>
 * Receive the setup message back on the return channel, check status
 * <p>
 * The return message contains a connectionID. This connectionID is used for all other commands pertaining to this mqtt
 * broker.
 * <p>
 * <b>Connect</b>
 * <p>
 * Create and setup an {@link MQTTConnectMessage} message.
 * <p>
 * Set the return channel and connectionID. Use the connectionID returned from the setup command.
 * <p>
 * For an INBOUND bridge (message sent from an mqtt topic to a channel), set the direction to
 * {@link MQTTConnectMessage#INBOUND} Inbound, the source to the mqtt topic, and the destination to the channel name.
 * <p>
 * For an OUTBOUND bridge (message sent from a channel to an mqtt topic), set the direction to
 * {@link MQTTConnectMessage#OUTBOUND} Outbound, the source to the channel name, and the destination to the mqtt topic.
 * <p>
 * Set start to true if you are ready to connect and begin the bridge. If you have more connections to setup for this
 * connectionID then set start to false. You must set start to true on the last MQTTConnectMessage to initiate the
 * connections.
 * <p>
 * Publish the connect message on the command channel (if not still open from the setup command, reopen the command
 * channel).
 * <p>
 * Receive the connect message back on the return channel, check status
 * <p>
 * When start is set to true (on the last connection), a separate thread is created and it connects the topics to
 * channels. At this point the bridge is in place, and messages from the specified topics and specified channels are
 * bridged.
 * <p>
 * <b>Close</b>
 * <p>
 * Create and setup an {@link MQTTCloseMessage} message.
 * <p>
 * Set the connectionID for the connections to close.
 * <p>
 * Publish the close message on the command channel (if not still open from the setup command, reopen the command
 * channel). Receive the close message back on the return channel, check status
 * <p>
 * <p>
 * See each message class for more info
 * <p>
 *
 */

@ModuleProperty(name = "MQTTBridge_Module", attributes = { AckChannelAttribute.class })
public class MQTTBridge_Module implements Module {
    public static final Logger logger = LogManager.getLogger(MQTTBridge_Module.class.getName());

    private Node node = null;
    private final Map<String, MqttWorkerRunnable> brokers = new HashMap<>();
    private RpcServer rpcServer;
    private URI ackChannelUri;
    private Attributes mqttAttributes;

    /**
     * @deprecated use {@link #run(Node, Attributes)} instead.
     */
    @Override
    @Deprecated
    public void run(Node node, Configuration moduleConfiguration, Channel<State> moduleStateChannel, long moduleId) {
        throw new UnsupportedOperationException(
                "Deprecated version of run, will be removed entirely in a future release");
    }

    /**
     * Run() method of the Module interface. Everything starts here. Open the well known
     * {@link MQTTCmdBaseMessage#COMMAND_CHANNEL_NAME} for this module and subscribe to it. When stopped, exit (see the
     * stop() method).
     */
    @Override
    public void run(Node node, Attributes attributes) {

        long moduleId;
        this.node = node;
        this.mqttAttributes = attributes;

        // TODO: This should come from the configuration channel.
        String bridgeCommandChannelName = node.getDefaultUri() + MQTTCmdBaseMessage.COMMAND_CHANNEL_NAME;

        // Subscribe on the MQTTCommand Channel and listen for commands
        logger.info("Open Channel: " + bridgeCommandChannelName);
        try {
            moduleId = mqttAttributes.get(IdAttribute.class);
            ackChannelUri = mqttAttributes.get(AckChannelAttribute.class);
            createServer(bridgeCommandChannelName, moduleId);
            logger.info("Channel Subscribed, ready for commands");
            setAttribute(ModuleStateAttribute.class, State.RUNNING);
            logger.debug("MQTT Module running, node={}, attributes={}, id={}", this.node, this.mqttAttributes,
                    moduleId);
        } catch (ChannelLifetimeException | ChannelIOException e) {
            logger.error("Failed to open bridge channel", e);
            setAttribute(ModuleStateAttribute.class, State.ERROR);
        } catch (MQTTBridgeModuleException e) {
            logger.error("RpcService exception occurred: ", e);
            setAttribute(ModuleStateAttribute.class, State.ERROR);
        } catch (AttributeNotFoundException e) {
            logger.error("Attribute not found", e);
            setAttribute(ModuleStateAttribute.class, State.ERROR);
        }
    }

    /**
     * Set an attribute with class and value pair with error handling
     *
     * @param attributeClass class of the attribute to be set
     * @param attributeValue value of the attribute to be set
     */
    private void setAttribute(Class attributeClass, Object attributeValue) {
        try {
            mqttAttributes.set(attributeClass, attributeValue);
        } catch (AttributeNotFoundException | AttributeNotWriteableException e) {
            logger.error("Attribute {} could not be set", attributeClass.getName(), e);
        }
    }

    /**
     * Adds commands to the CommandRegistry and creates a new RpcServer instance to create a listening channel.
     *
     * @param moduleCommandChannelName command channel name
     */
    private void createServer(String moduleCommandChannelName, long moduleId)
            throws ChannelIOException, ChannelLifetimeException, MQTTBridgeModuleException {
        rpcServer = Rpc.newServer(node.channels(),
                ChannelUtils.join(URI.create(moduleCommandChannelName), Long.toString(moduleId)));
        CommandAdapter commandAdapter = new CommandAdapter(this);
        try {
            rpcServer.registry().add(commandAdapter.toCommand("mqttSetup"));
            rpcServer.registry().add(commandAdapter.toCommand("mqttConnect"));
            rpcServer.registry().add(commandAdapter.toCommand("mqttClose"));
            rpcServer.registry().list().forEach(logger::info);
        } catch (NoSuchMethodException e) {
            throw new MQTTBridgeModuleException(e.getMessage());
        }

        rpcServer.serve();
    }

    /**
     * Returns the WorkerRunnable that is setup to handle connections. This runnable is created via the connect command.
     *
     * @param connectionId The specific ID for the connection, returned via the setup command.
     * @return The runnable that is responsible for the connections on this connectionID.
     */
    public MqttWorkerRunnable getBroker(String connectionId) {
        return brokers.get(connectionId);
    }

    /**
     * Returns the RpcServer that handles the command channel.
     *
     * @return RpcServer instance
     */
    public RpcServer getRpcServer() {
        return rpcServer;
    }

    /**
     * Returns the module name.
     *
     * @return module name
     */
    public String getName() {
        return node.getName();
    }

    /**
     * Adds a connectionID and runnable for a given connection. This is called via the setup command.
     *
     * @param message The setup message containing the connectionID.
     */
    public void putBroker(MQTTSetupMessage message) {
        brokers.put(message.connectionID, new MqttWorkerRunnable(node, message, ackChannelUri));
    }

    /**
     * State channel stopped by Module Manager
     */
    @Override
    public void stop(StopReason reason) {
        try {
            closeAllChannels();
            if (rpcServer != null) {
                rpcServer.close();
            }
            setAttribute(ModuleStateAttribute.class, State.STOPPED);
        } catch (ChannelLifetimeException e) {
            logger.error("Failed to close bridgeChannel", e);
            setAttribute(ModuleStateAttribute.class, State.ERROR);
        }
    }

    /**
     * close all channels open by the MQTT module
     */
    private void closeAllChannels() {
        Map<String, MqttWorkerRunnable> closedBrokers = new HashMap<>();
        for (Entry<String, MqttWorkerRunnable> broker : brokers.entrySet()) {
            broker.getValue().cleanupConnections();
            closedBrokers.put(broker.getKey(), broker.getValue());
        }
        for (Entry<String, MqttWorkerRunnable> broker : closedBrokers.entrySet()) {
            brokers.remove(broker.getKey());
        }
    }

    /**
     * Get all channels held by the module
     *
     * @return list of open channels
     */
    public Collection<String> getAllBrokers() {
        return brokers.entrySet().stream().map(Object::toString).collect(Collectors.toCollection(ArrayList::new));
    }
}
