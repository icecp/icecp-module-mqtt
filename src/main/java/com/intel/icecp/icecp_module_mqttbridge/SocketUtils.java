package com.intel.icecp.icecp_module_mqttbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class SocketUtils {
	static final Logger LOGGER = LogManager.getFormatterLogger(SocketUtils.class.getName());

	private SocketUtils () {}
	
	public static void setMqttSocketFactory(MqttConnectOptions s, String mqttBrokerAddress, String proxyHost, int proxyPort) {

		URI mqttBrokerUri;
		try {
			mqttBrokerUri = new URI(mqttBrokerAddress);
		}
		catch (URISyntaxException se) {
			LOGGER.error("Broker %s not a valid URI (%s)", mqttBrokerAddress, se.getMessage());
			return;
		}
		
		if (proxyHost != null && !proxyHost.trim().isEmpty()) {
			LOGGER.info("MQTT Broker %s request SOCKS proxy %s:%d", mqttBrokerAddress, proxyHost, proxyPort);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));

			if ("ssl".equalsIgnoreCase(mqttBrokerUri.getScheme())) {
				s.setSocketFactory(new SSLProxySocketFactory(proxy));
			} else {
				s.setSocketFactory(new ProxySocketFactory(proxy));
			}
		} else {
			if ("ssl".equalsIgnoreCase(mqttBrokerUri.getScheme())) {
				s.setSocketFactory(getMqttSocketFactory());
			}
		}
	}

	static SSLSocketFactory getMqttSocketFactory() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
			return sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			LOGGER.warn("Cannot get TLS 1.2 factory or key management error", e);
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
	}

}

@SuppressWarnings("serial")
class UnimplementedException extends Exception {
	public UnimplementedException () {
		super ("Unimplemented Exception");
	}
}

// Just an interceptor class in case proxy behavior needs to be overwritten
class ProxySocket extends Socket {
	public ProxySocket(Proxy proxy) {
		super(proxy);
	}
}

class ProxySocketFactory extends SocketFactory {
	private static final Logger LOGGER = LogManager.getFormatterLogger(SocketUtils.class.getName());
	SocketFactory factory;
	Proxy proxy;

	public ProxySocketFactory(Proxy proxy) {
		factory = SocketFactory.getDefault();
		this.proxy = proxy;
	}

	@Override
	public Socket createSocket() throws IOException {
		return new ProxySocket(proxy);
	}

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}
}

class ProxySSLSocket extends SSLSocket {
	SSLSocketFactory master;
	SSLSocket socket;
	Socket proxy;

	public ProxySSLSocket(Socket proxy, SSLSocketFactory master) {
		this.master = master;
		this.proxy = proxy;
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		InetSocketAddress sa = (InetSocketAddress) endpoint;
		proxy.connect(endpoint, timeout);
		socket = (SSLSocket) master.createSocket(proxy, sa.getHostString(), sa.getPort(), true);
	}

	@Override
	public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
		socket.addHandshakeCompletedListener(listener);
	}

	@Override
	public boolean getEnableSessionCreation() {
		return socket.getEnableSessionCreation();
	}

	@Override
	public String[] getEnabledCipherSuites() {
		return socket.getEnabledCipherSuites();
	}

	@Override
	public String[] getEnabledProtocols() {
		return socket.getEnabledProtocols();
	}

	@Override
	public boolean getNeedClientAuth() {
		return socket.getNeedClientAuth();
	}

	@Override
	public SSLSession getSession() {
		return socket.getSession();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return socket.getSupportedCipherSuites();
	}

	@Override
	public String[] getSupportedProtocols() {
		return socket.getSupportedProtocols();
	}

	@Override
	public boolean getUseClientMode() {
		return socket.getUseClientMode();
	}

	@Override
	public boolean getWantClientAuth() {
		return socket.getWantClientAuth();
	}

	@Override
	public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
		socket.removeHandshakeCompletedListener(listener);
	}

	@Override
	public void setEnableSessionCreation(boolean flag) {
		socket.setEnableSessionCreation(flag);
	}

	@Override
	public void setEnabledCipherSuites(String[] suites) {
		socket.setEnabledCipherSuites(suites);
	}

	@Override
	public void setEnabledProtocols(String[] protocols) {
		socket.setEnabledProtocols(protocols);
	}

	@Override
	public void setNeedClientAuth(boolean need) {
		socket.setNeedClientAuth(need);
	}

	@Override
	public void setUseClientMode(boolean mode) {
		socket.setUseClientMode(mode);
	}

	@Override
	public void setWantClientAuth(boolean want) {
		socket.setWantClientAuth(want);
	}

	@Override
	public void startHandshake() throws IOException {
		socket.startHandshake();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

}

class SSLProxySocketFactory extends SSLSocketFactory {
	private static final Logger LOGGER = LogManager.getFormatterLogger(SocketUtils.class.getName());

	SSLSocketFactory master;
	Proxy proxy;

	public SSLProxySocketFactory(Proxy proxy) {
		this.master = SocketUtils.getMqttSocketFactory();
		this.proxy = proxy;
	}

	@Override
	public Socket createSocket() throws IOException {
		return new ProxySSLSocket(new Socket(proxy), master);
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return master.createSocket(s, host, port, autoClose);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return master.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return master.getDefaultCipherSuites();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		LOGGER.catching(new UnimplementedException());
		return null;
	}

}
