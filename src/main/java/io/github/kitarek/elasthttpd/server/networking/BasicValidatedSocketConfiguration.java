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


import io.github.kitarek.elasthttpd.commons.Optional;

import java.net.InetAddress;

import static io.github.kitarek.elasthttpd.commons.Optional.optional;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

public class BasicValidatedSocketConfiguration implements SocketConfiguration {

	public static final int MAX_PORT_NUMBER = 65535;
	public static final int MIN_PORT_NUMBER = 1;
	public static final int DEFAULT_MAXIMUM_NUMBER_OF_CONNECTIONS_IN_QUEUE = 1000;
	private final InetAddress listeningAddress;
	private final int listeningPort;
	private int connectionsToAcceptQueueSize;
	private Integer socketReceiveBufferSizeInBytes;
	private Integer socketSendBufferSizeInBytes;
	private KeepAliveMode keepAlivePacketsMode;
	private SmallerPacketsSendingPolicy smallerPacketsSendingPolicy;
	private AddressAndPortReusePolicy addressAndPortReusePolicy;
	private Integer socketTimeoutInMiliseconds;

	public BasicValidatedSocketConfiguration(InetAddress listeningAddress, int listeningPort) {
		this.listeningAddress = notNull(listeningAddress, "The IP address cannot be null");

		inclusiveBetween(MIN_PORT_NUMBER, MAX_PORT_NUMBER, listeningPort,
				format("The port needs to be specified as number between %d and %d", MIN_PORT_NUMBER, MAX_PORT_NUMBER));
		this.listeningPort = listeningPort;
		this.connectionsToAcceptQueueSize = DEFAULT_MAXIMUM_NUMBER_OF_CONNECTIONS_IN_QUEUE;
	}

	public BasicValidatedSocketConfiguration(InetAddress listeningAddress, int listeningPort, int connectionsToAcceptQueueSize) {
		this(listeningAddress, listeningPort);
		inclusiveBetween(1, Integer.MAX_VALUE, connectionsToAcceptQueueSize,
				"The connectionsToAcceptQueueSize needs to be higher than 1");
		this.connectionsToAcceptQueueSize = connectionsToAcceptQueueSize;
	}

	public BasicValidatedSocketConfiguration(InetAddress listeningAddress, int listeningPort, int connectionsToAcceptQueueSize,
											 Integer socketReceiveBufferSizeInBytes, Integer socketSendBufferSizeInBytes,
											 KeepAliveMode keepAlivePacketsMode,
											 SmallerPacketsSendingPolicy smallerPacketsSendingPolicy,
											 Integer socketTimeoutInMiliseconds,
											 AddressAndPortReusePolicy addressAndPortReusePolicy) {
		this(listeningAddress, listeningPort, connectionsToAcceptQueueSize);

		if (socketReceiveBufferSizeInBytes != null)
			inclusiveBetween(1, Integer.MAX_VALUE, socketReceiveBufferSizeInBytes, "Socket receive buffer size needs to be greater than 0");
		this.socketReceiveBufferSizeInBytes = socketReceiveBufferSizeInBytes;

		if (socketSendBufferSizeInBytes != null)
			inclusiveBetween(1, Integer.MAX_VALUE, socketSendBufferSizeInBytes, "Socket send buffer size needs to be greater than 0");
		this.socketSendBufferSizeInBytes = socketSendBufferSizeInBytes;

		if (socketTimeoutInMiliseconds != null)
			inclusiveBetween(1, Integer.MAX_VALUE, socketTimeoutInMiliseconds, "Socket timeout in miliseconds needs to be greater than 0");
		this.socketTimeoutInMiliseconds = socketTimeoutInMiliseconds;

		this.keepAlivePacketsMode = keepAlivePacketsMode;
		this.smallerPacketsSendingPolicy = smallerPacketsSendingPolicy;
		this.addressAndPortReusePolicy = addressAndPortReusePolicy;
	}

	public InetAddress getListeningAddress() {
		return listeningAddress;
	}

	public int getListeningPort() {
		return listeningPort;
	}

	public int getConnectionsToAcceptQueueSize() {
		return connectionsToAcceptQueueSize;
	}

	public Optional<Integer> getSocketReceiveBufferSizeInBytes() {
		return optional(socketReceiveBufferSizeInBytes);
	}

	public Optional<Integer> getSocketSendBufferSizeInBytes() {
		return optional(socketSendBufferSizeInBytes);
	}

	public Optional<KeepAliveMode> getKeepAlivePacketsMode() {
		return optional(keepAlivePacketsMode);
	}

	public Optional<SmallerPacketsSendingPolicy> getSmallerPacketsSendingPolicy() {
		return optional(smallerPacketsSendingPolicy);
	}

	public Optional<Integer> getSocketTimeoutInMiliseconds() {
		return optional(socketTimeoutInMiliseconds);
	}

	public Optional<AddressAndPortReusePolicy> getAddressAndPortReusePolicy() {
		return optional(addressAndPortReusePolicy);
	}
}
