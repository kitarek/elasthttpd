/*
 * Copyright 2015 Arek Kita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.kitarek.elasthttpd.server.networking;

import java.net.Inet4Address;
import java.net.InetAddress;

import static io.github.kitarek.elasthttpd.server.networking.SocketConfiguration.MAX_PORT_NUMBER;
import static io.github.kitarek.elasthttpd.server.networking.SocketConfiguration.MIN_PORT_NUMBER;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

public class NetworkConfigurationBuilder {
	public static final int DEFAULT_LISTEN_PORT = 18181;
	public static final int DEFAULT_NOT_YET_ACCEPTED_CONNECTION_QUEUE_SIZE = 1024;
	public static final InetAddress DEFAULT_IPV4_LOOPBACK_LISTEN_ADDRESS = Inet4Address.getLoopbackAddress();
	private InetAddress listeningAddress;
	private int listeningPort;
	private int connectionsToAcceptQueueSize;
	private Integer socketReceiveBufferSizeInBytes;
	private Integer socketSendBufferSizeInBytes;
	private KeepAliveMode keepAlivePacketsMode;
	private SmallerPacketsSendingPolicy smallerPacketsSendingPolicy;
	private Integer socketTimeoutInMiliseconds;
	private AddressAndPortReusePolicy addressAndPortReusePolicy;

	private NetworkConfigurationBuilder() {}

	public static NetworkConfigurationBuilder newConfiguration() {
		final NetworkConfigurationBuilder builder = new NetworkConfigurationBuilder();
		builder.setListeningPort(DEFAULT_LISTEN_PORT);
		builder.setConnectionsToAcceptQueueSize(DEFAULT_NOT_YET_ACCEPTED_CONNECTION_QUEUE_SIZE);
		builder.setListeningAddress(DEFAULT_IPV4_LOOPBACK_LISTEN_ADDRESS);
		return builder;
	}

	public NetworkConfigurationBuilder setListeningAddress(InetAddress listeningAddress) {
		this.listeningAddress = notNull(listeningAddress, "Listening address cannot be null");
		return this;
	}

	public NetworkConfigurationBuilder setListeningPort(int listeningPort) {
		inclusiveBetween(MIN_PORT_NUMBER, MAX_PORT_NUMBER, listeningPort,
				format("Port number must be a value between %d and %d", MIN_PORT_NUMBER, MAX_PORT_NUMBER));
		this.listeningPort = listeningPort;
		return this;
	}

	public NetworkConfigurationBuilder setConnectionsToAcceptQueueSize(int connectionsToAcceptQueueSize) {
		inclusiveBetween(1, MAX_VALUE, connectionsToAcceptQueueSize,
				format("Queue size must be a number between %d and %d", 1, MAX_VALUE));
		this.connectionsToAcceptQueueSize = connectionsToAcceptQueueSize;
		return this;
	}

	public NetworkConfigurationBuilder setSocketReceiveBufferSizeInBytes(Integer socketReceiveBufferSizeInBytes) {
		notNull(socketReceiveBufferSizeInBytes, "The receive buffer size needs to be not null");
		inclusiveBetween(1, MAX_VALUE, socketReceiveBufferSizeInBytes,
				format("Receive buffer size must be a number between %d and %d", 1, MAX_VALUE));
		this.socketReceiveBufferSizeInBytes = socketReceiveBufferSizeInBytes;
		return this;
	}

	public NetworkConfigurationBuilder setSocketSendBufferSizeInBytes(Integer socketSendBufferSizeInBytes) {
		notNull(socketSendBufferSizeInBytes, "The send buffer size needs to be not null");
		inclusiveBetween(1, MAX_VALUE, socketSendBufferSizeInBytes,
				format("Send buffer size must be a number between %d and %d", 1, MAX_VALUE));
		this.socketSendBufferSizeInBytes = socketSendBufferSizeInBytes;
		return this;
	}

	public NetworkConfigurationBuilder setKeepAlivePacketsMode(KeepAliveMode keepAlivePacketsMode) {
		this.keepAlivePacketsMode = notNull(keepAlivePacketsMode, "The Keep Alive Packets Mode must be not null");
		return this;
	}

	public NetworkConfigurationBuilder setSmallerPacketsSendingPolicy(SmallerPacketsSendingPolicy smallerPacketsSendingPolicy) {
		this.smallerPacketsSendingPolicy = notNull(smallerPacketsSendingPolicy, "The Smaller Packets Sending Policy must be not null");
		return this;
	}

	public NetworkConfigurationBuilder setSocketTimeoutInMiliseconds(Integer socketTimeoutInMiliseconds) {
		notNull(socketTimeoutInMiliseconds, "The socket timeout needs to be not null");
		inclusiveBetween(1, MAX_VALUE, socketTimeoutInMiliseconds,
				format("Socket timeout must be a number between %d and %d", 1, MAX_VALUE));
		this.socketTimeoutInMiliseconds = socketTimeoutInMiliseconds;
		return this;
	}

	public NetworkConfigurationBuilder setAddressAndPortReusePolicy(AddressAndPortReusePolicy addressAndPortReusePolicy) {
		this.addressAndPortReusePolicy = notNull(addressAndPortReusePolicy, "The Address and Port Reuse Policy must be not null");
		return this;
	}

	public SocketConfiguration createNow() {
		return new BasicValidatedSocketConfiguration(listeningAddress, listeningPort, connectionsToAcceptQueueSize,
				socketReceiveBufferSizeInBytes, socketSendBufferSizeInBytes, keepAlivePacketsMode,
				smallerPacketsSendingPolicy, socketTimeoutInMiliseconds, addressAndPortReusePolicy);
	}
}