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
import io.github.kitarek.elasthttpd.commons.OptionalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import static io.github.kitarek.elasthttpd.commons.Optional.empty;
import static io.github.kitarek.elasthttpd.commons.Optional.present;
import static io.github.kitarek.elasthttpd.server.networking.AddressAndPortReusePolicy.*;
import static org.apache.commons.lang3.Validate.notNull;

public class HttpConfiguredServerSocket implements ListeningSocket {

	public static final Logger logger = LoggerFactory.getLogger(HttpConfiguredServerSocket.class);
	private final ServerSocket serverSocket;
	private final SocketConfiguration socketConfiguration;

	public static HttpConfiguredServerSocket newHttpConfiguredServerSocket(final SocketConfiguration socketConfiguration) {
		notNull(socketConfiguration, "Socket configuration cannot be not null");
		return new HttpConfiguredServerSocket(socketConfiguration, createServerSocket(socketConfiguration));
	}

	private static ServerSocket createServerSocket(SocketConfiguration socketConfiguration) {
		try {
			return createServerSocketUnchecked(socketConfiguration);
		} catch (Exception e) {
			logger.error("An error creating server socket", e);
			throw new IllegalStateException(e);
		}
	}

	private static ServerSocket createServerSocketUnchecked(final SocketConfiguration socketConfiguration) throws IOException {
		ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
		final ServerSocket serverSocket = serverSocketFactory.createServerSocket(
				socketConfiguration.getListeningPort(), socketConfiguration.getConnectionsToAcceptQueueSize(),
				socketConfiguration.getListeningAddress());
		setSocketReceiveBufferSizeWhenProvided(socketConfiguration, serverSocket);
		setSocketReuseAddress(socketConfiguration, serverSocket);
		return serverSocket;
	}

	private static void setSocketReceiveBufferSizeWhenProvided(final SocketConfiguration socketConfiguration, final ServerSocket serverSocket) {
		socketConfiguration.getSocketReceiveBufferSizeInBytes().map(new OptionalMapper<Integer>() {
			public void present(Integer receiveBufferSize) {
				try {
					serverSocket.setReceiveBufferSize(receiveBufferSize);
				} catch (SocketException e) {
					throw new IllegalStateException("Cannot set socket receive buffer size", e);
				}
			}
		});
	}

	private static void setSocketReuseAddress(final SocketConfiguration socketConfiguration, final ServerSocket serverSocket) {
		socketConfiguration.getAddressAndPortReusePolicy().map(new OptionalMapper<AddressAndPortReusePolicy>() {
			public void present(AddressAndPortReusePolicy addressAndPortReusePolicy) {
				try {
					serverSocket.setReuseAddress(
							addressAndPortReusePolicy == REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS);
				} catch (SocketException e) {
					throw new IllegalStateException("Cannot set socket receive buffer size", e);
				}
			}
		});
	}

	private HttpConfiguredServerSocket(final SocketConfiguration socketConfiguration, final ServerSocket serverSocket) {
		this.socketConfiguration = notNull(socketConfiguration, "Socket configuration cannot be not null");
		this.serverSocket = notNull(serverSocket, "Server socket cannot be null");
	}

	public Optional<NewConnection> listenForANewConnection() {
		try {
			final NewConnection newConnection = new HttpNewConnection(serverSocket.accept(), socketConfiguration);
			return present(newConnection);
		} catch (IOException e) {
			return reactOnInputOutputListenError(e);
		}
	}

	private Optional<NewConnection> reactOnInputOutputListenError(IOException e) {
		if (!serverSocket.isClosed()) {
			logger.error("An I/O error occured when accepting connection", e);
			throw new IllegalStateException(e);
		} else {
			return empty();
		}
	}

	public void stopListening() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("An I/O error occured when closing socket", e);
		}
	}
}
