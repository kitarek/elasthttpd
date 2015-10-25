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

/**
 * Provide configuration for client and server sockets
 */
public interface SocketConfiguration {
	/**
	 * Maximal achievable port number that can be bound by server socket
	 */
	int MAX_PORT_NUMBER = 65535;

	/**
	 * Minimal achievable port number that can be bound by server socket
	 */
	int MIN_PORT_NUMBER = 1;

	/**
	 * Internet address on which server socket is listening
	 *
	 * @return always not null reference
	 */
	InetAddress getListeningAddress();

	/**
	 * Port number on which server socket is listening for specified Internet Address by {@link #getListeningAddress()}
	 *
	 * @return valid port number from 1 to 65535
	 */
	int getListeningPort();

	/**
	 * This is the incoming connections queue size (number of connections) not yet accepted - waiting to be accepted
	 * by server socket.
	 *
	 * @return maximum number of connections that can be hold without rejection before acceptance.
	 */
	int getConnectionsToAcceptQueueSize();

	/**
	 * Get size of packet <b>receive</b> buffer number of bytes. Otherwise if value is not provided the system default
	 * will be used for socket.
	 * @return number of bytes
	 */
	Optional<Integer> getSocketReceiveBufferSizeInBytes();

	/**
	 * Get size of packet <b>send</b> buffer number of bytes. Otherwise if value is not provided the system default
	 * will be used for socket.
	 * @return number of bytes
	 */
	Optional<Integer> getSocketSendBufferSizeInBytes();

	/**
	 * Get mode that will be used for special keep alive packets. Otherwise if value is not provided the system default
	 * will be used for socket.
	 *
	 * @return enum that represents keep alive mode if present
	 */
	Optional<KeepAliveMode> getKeepAlivePacketsMode();

	/**
	 * Get policy for sending smaller packets. Otherwise if value is not provided the system default
	 * will be used for socket.
	 *
	 * @return enum value that represents one of available policy option for sending smaller packets if present
	 */
	Optional<SmallerPacketsSendingPolicy> getSmallerPacketsSendingPolicy();

	/**
	 * Maximum time period reserved for connecting to server or before closing idle connection. Otherwise if value is
	 * not provided the system default will be used for socket.
	 *
	 * @return number of miliseconds if present
	 */
	Optional<Integer> getSocketTimeoutInMiliseconds();

	/**
	 * Get policy for quick or safe socket binding. Otherwise if value is
	 * not provided the system default will be used for socket.
	 *
	 * @return chosen policy for binding server sockets (if present)
	 */
	Optional<AddressAndPortReusePolicy> getAddressAndPortReusePolicy();
}
