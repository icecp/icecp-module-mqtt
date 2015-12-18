package com.intel.icecp.icecp_module_mqttbridge;

import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Message;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.messages.BytesMessage;
import com.intel.icecp.core.metadata.Persistence;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.misc.OnPublish;
import com.intel.icecp.icecp_module_mqttbridge.ack.AckMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTConnectMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTMessage;
import com.intel.icecp.icecp_module_mqttbridge.message.MQTTSetupMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * The MQTT Worker Runnable handles the bridge connections.
 * <p>
 * This runnable is responsible for connecting the endpoints for a connectionID. It connects MQTT topics to channels and
 * vice versus. When a connect command is received by the module, it hands the connection information to this runnable.
 * When the start flag of the connect {@link MQTTConnectMessage} command is set to true, then the actual connections are
 * made.
 * <p>
 *
 */
public class MqttWorkerRunnable implements Runnable, MqttCallback {
    private static final int RECONNECT_SLEEP = 30;

    private static final Logger LOGGER = LogManager.getLogger(MqttWorkerRunnable.class.getName());
    private Semaphore terminateThread = new Semaphore(0);

    private Node node = null;
    private String brokerURL, clientId;
    private String brokerUser, brokerPassword;
    private String proxyHost;
    private int proxyPort;
    private URI ackChannelUri;
    private MessageDigest digest;

    private MqttClient client = null;
    private MqttConnectOptions connectOptions;

    // All connections, pub and sub
    private Set<Connection> connectionSet = new HashSet<>();

    /**
     * The node is passed in and the original setup message {@link MQTTSetupMessage}. Parameters from the setup message
     * are stored for use when the connections are made.
     *
     * @param node The node for accessing channels
     * @param setupMessage The original setup message for the connectionID
     * @param ackChannelUri ack channel URI
     */
    public MqttWorkerRunnable(Node node, MQTTSetupMessage setupMessage, URI ackChannelUri) {
        this.node = node;
        this.ackChannelUri = ackChannelUri;
        brokerURL = setupMessage.brokerURL;
        clientId = setupMessage.clientId;
        brokerUser = setupMessage.brokerUser;
        brokerPassword = setupMessage.brokerPassword;

        proxyHost = setupMessage.proxyHost;
        proxyPort = setupMessage.proxyPort;

        // TODO:  Remove this temporary try/catch once ID attributes is completed.
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to create MessageDigest", e);
        }

        createClient();
    }

    /**
     * Create the client for broker and clientID, set this class as callback
     */
    private void createClient() {
        try {
            client = new MqttClient(brokerURL, clientId, new MemoryPersistence());
            client.setCallback(this);
            connectOptions = new MqttConnectOptions();
        } catch (MqttException e) {
            throw new RuntimeException("Failed to register with the MQTT broker: ", e);
        }
    }

    /**
     * Builder pattern to add subscriptions and publications
     *
     * @param icecpSourceChannel Source channel
     * @param mqttTopic MQTT topic
     * @return this{@link MqttWorkerRunnable}
     */
    public MqttWorkerRunnable addOutboundConnection(String icecpSourceChannel, String mqttTopic, boolean wrapPayload) {

        synchronized (connectionSet) {
            connectionSet.add(new Connection().createPublication(icecpSourceChannel, mqttTopic, wrapPayload));
        }
        return this;
    }

    public MqttWorkerRunnable addOutboundAckConnection(String icecpSourceChannel, String mqttTopic, boolean wrapPayload) {

        synchronized (connectionSet) {
            connectionSet.add(new Connection().createPublicationWithAck(icecpSourceChannel, mqttTopic, wrapPayload));
        }
        return this;
    }

    public MqttWorkerRunnable addInboundConnection(String mqttTopic, String icecpDestChannel, boolean wrapPayload) {
        Connection con;

        synchronized (connectionSet) {
            con = new Connection().createSubscription(mqttTopic, icecpDestChannel, wrapPayload);
            connectionSet.add(con);
        }
        // If we add an inbound connection while being connected, add subscription
        if (client.isConnected()) {
            subscribeMqttTopic(con);
        }
        return this;
    }

    /**
     * Subscribe to a MQTT Broker Topics
     *
     * @return true if it subscribed to all topics
     */
    private boolean subscribeAllMqttTopics() {
        boolean allSubscribed = true;
        synchronized (connectionSet) {
            for (Connection con : connectionSet) {
                if (con.isSubscription) {
                    allSubscribed &= subscribeMqttTopic(con);
                }
            }
        }
        return allSubscribed;
    }

    /**
     * subscribe to topic
     *
     * @param subCon must be a subscription (ie, isSubscription == true)
     */
    private boolean subscribeMqttTopic(Connection subCon) {
        try {
            client.subscribe(subCon.mqttTopic);
            LOGGER.info("Subscribed to topic: " + subCon.mqttTopic);
            return true;
        } catch (MqttException e) {
            LOGGER.error("Cannot subscribe to topic: " + subCon.mqttTopic, e);
            return false;
        }
    }

    /**
     * Connect the client and establishes all subscriptions.
     *
     * @throws MqttException MqttException if connection to MQTT broker fails
     */
    private void connectClient() throws MqttException {
        connectOptions.setCleanSession(true);
        connectOptions.setConnectionTimeout(10);
        connectOptions.setKeepAliveInterval(5);

        if (brokerUser != null && !brokerUser.isEmpty()) {
            connectOptions.setUserName(brokerUser);
            connectOptions.setPassword(brokerPassword.toCharArray());
        }
        // It's the clients responsibility to check -Dsocks.proxyHost=proxy-us.intel.com -Dsocks.proxyPort=1080
        SocketUtils.setMqttSocketFactory(connectOptions, client.getServerURI(), proxyHost, proxyPort);

        client.connect(connectOptions);
        subscribeAllMqttTopics();
    }

    /**
     * Close all channels for inbound and outbound connections.
     */
    public void cleanupConnections() {
        synchronized (connectionSet) {
            for (Connection con : connectionSet) {
                try {
                    (con.isSubscription ? con.icecpDestChannel : con.icecpSourceChannel).close();
                } catch (ChannelLifetimeException e) {
                    LOGGER.debug("Error closing channel", e);
                }
            }
        }
    }

    /**
     * The run method. When this runnable starts up, it starts here. Connect to the client and subscribe to the
     * subscriptions (topics). The publications (channels) are already subscribed to. Then wait to be stopped.
     */
    @Override
    public void run() {
        try {
            connectClient();

            // Now wait until this terminates
            terminateThread.acquireUninterruptibly();

            client.disconnectForcibly(0);
            client.close();
            cleanupConnections();
        } catch (MqttException e) {
            LOGGER.error("MQTT runtime exception in worker thread", e);
        }
    }

    /**
     * This implements {@link MqttCallback#connectionLost(Throwable)}
     *
     * @param t Throwable error
     */
    @Override
    public void connectionLost(Throwable t) {
        LOGGER.warn("Connection to MQTT broker lost");
        while (true) {
            try {
                LOGGER.info("Attempting to reconnect");
                connectClient();
                LOGGER.info("Successfully reconnected");
                return;
            } catch (MqttException e) {
                LOGGER.warn("Reconnect attempt failed. Retry in {} seconds", RECONNECT_SLEEP, e);
                try {
                    Thread.sleep(1000L * RECONNECT_SLEEP);
                } catch (InterruptedException e1) {
                    Thread.interrupted();
                }
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        synchronized (connectionSet) {
            for (Connection con : connectionSet) {
                if (con.isSubscription && TopicMatcher.isTopicSubscribed(con.mqttTopic, topic)) {
                    LOGGER.info("Received MQTT message on subscribed topic " + topic);
                    if (con.wrapPayload)
                        con.icecpDestChannel.publish(new MQTTMessage(message));
                    else
                        con.icecpDestChannel.publish(new BytesMessage(message.getPayload()));
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO: Implement a tracking method if we need
    }

    @Override
    public String toString() {
        return String.format("MQTT module worker [%s:%s]", brokerURL, clientId);
    }

    public void terminateThread() {
        terminateThread.release(Integer.MAX_VALUE);
    }

    /**
     * Connection Management Class
     *
     */
    class Connection {
        boolean wrapPayload;
        boolean isSubscription;
        String mqttTopic;
        Channel<BytesMessage> icecpSourceChannel;
        Channel<Message> icecpDestChannel;
        Channel<AckMessage> ackMessageChannel = null;

        public Connection createSubscription(String mqttTopic, String icecpDestChannel, boolean wrapPayload) {
            this.wrapPayload = wrapPayload;
            isSubscription = true;
            this.mqttTopic = mqttTopic;
            try {
                this.icecpDestChannel = node.openChannel(new URI(icecpDestChannel), Message.class, new Persistence());
            } catch (ChannelLifetimeException | URISyntaxException e) {
                LOGGER.warn("createSubscription exception", e);
            }
            return this;
        }

        public Connection createPublication(String icecpSourceChannel, String mqttTopic, boolean wrapPayload) {
            setPublicationValues(wrapPayload, mqttTopic);
            try {
                this.icecpSourceChannel = node.openChannel(new URI(icecpSourceChannel), BytesMessage.class,
                        new Persistence());
                this.icecpSourceChannel.subscribe(new OnPublish<BytesMessage>() {
                    @Override
                    public synchronized void onPublish(BytesMessage bytesMessage) {
                        try {
                            publishMqttMessage(bytesMessage);
                        } catch (MqttPersistenceException e1) {
                            LOGGER.error("Publish to Broker, Persistence error: ", e1);
                        } catch (MqttException e2) {
                            LOGGER.error("Publish to Broker, MqttException: ", e2);
                        }
                    }
                });
            } catch (ChannelLifetimeException | ChannelIOException | URISyntaxException e) {
                LOGGER.warn("Channel Lifetime Error or IO Exception", e);
            }
            return this;
        }


        public Connection createPublicationWithAck(String icecpSourceChannel, String mqttTopic, boolean wrapPayload) {
            setPublicationValues(wrapPayload, mqttTopic);
            try {
                this.icecpSourceChannel = node.openChannel(new URI(icecpSourceChannel), BytesMessage.class,
                        new Persistence());

                if (this.ackMessageChannel == null) {
                    this.ackMessageChannel = node.openChannel(ackChannelUri, AckMessage.class, Persistence.DEFAULT);
                }
                this.icecpSourceChannel.subscribe(new OnPublish<BytesMessage>() {
                    @Override
                    public synchronized void onPublish(BytesMessage bytesMessage) {
                        try {
                            publishMqttMessage(bytesMessage);
                            createAndSendAcknowledgement(icecpSourceChannel, bytesMessage);
                        } catch (MqttPersistenceException e1) {
                            LOGGER.error("Publish to Broker, Persistence error: ", e1);
                        } catch (MqttException e2) {
                            LOGGER.error("Publish to Broker, MqttException: ", e2);
                        }
                    }
                });

            } catch (URISyntaxException | ChannelLifetimeException | ChannelIOException e) {
                LOGGER.warn("Channel Lifetime Error or IO Exception", e);
            }
            return this;
        }

        private void setPublicationValues(boolean wrapPayload, String mqttTopic) {
            this.wrapPayload = wrapPayload;
            isSubscription = false;
            this.mqttTopic = mqttTopic;
        }

        private void publishMqttMessage(BytesMessage message) throws MqttException {
            MqttMessage msg = new MqttMessage(message.getBytes());
            // TODO: This QoS should be configurable and set by the user through the MQTTConnect options in the python script
            msg.setQos(2);
            msg.setRetained(true);

            LOGGER.info("Publish on Topic[{}], Msg[{}]", mqttTopic, msg);
            client.publish(mqttTopic, msg);
        }

        // TO-DO All Ack related methods in MQTT and TAP modules are a replication of Ack Module functionality.
        // Ack related methods should be moved to a library
        private void createAndSendAcknowledgement(String icecpSourceChannel, BytesMessage message) {
            long id = toAcknowledgmentId(hashMessageContent(message));
            AckMessage ackMessage = new AckMessage(icecpSourceChannel, id);
            try {
                if (ackMessageChannel != null)
                    ackMessageChannel.publish(ackMessage);
                else
                    LOGGER.error("MQTT Ack channel is null! Channel has not been opened: {}", ackChannelUri);
            } catch (ChannelIOException e) {
                LOGGER.error("Failed to send MQTT acknowledgement with id: {}", id, e);
            }
        }

        private byte[] hashMessageContent(BytesMessage message) {
            if (digest != null) {
                digest.update(message.getBytes());
                byte[] returnBytes = digest.digest();
                digest.reset();
                return returnBytes;
            } else {
                UUID id = UUID.randomUUID();
                ByteBuffer buf = ByteBuffer.wrap(new byte[16]);
                buf.putLong(id.getMostSignificantBits());
                buf.putLong(id.getLeastSignificantBits());
                return buf.array();
            }
        }

        private long toAcknowledgmentId(byte[] persistentMessageId) {
            return Arrays.hashCode(persistentMessageId);
        }
    }
}