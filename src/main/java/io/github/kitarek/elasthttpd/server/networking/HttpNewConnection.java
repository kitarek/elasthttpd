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

import io.github.kitarek.elasthttpd.commons.OptionalMapper;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import static io.github.kitarek.elasthttpd.server.networking.KeepAliveMode.*;
import static io.github.kitarek.elasthttpd.server.networking.SmallerPacketsSendingPolicy.*;
import static org.apache.commons.lang3.Validate.notNull;

public class HttpNewConnection implements NewConnection {

	public static final Logger logger = LoggerFactory.getLogger(HttpNewConnection.class);
	private final Socket clientSocket;
	private final SocketConfiguration socketConfiguration;
	private final HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;

	public HttpNewConnection(Socket clientSocket, SocketConfiguration socketConfiguration) {
		this.clientSocket = notNull(clientSocket, "Client socket cannot be null");
		this.socketConfiguration = notNull(socketConfiguration, "Socket configuration cannot be null");
		connectionFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
	}

	public HttpServerConnection acceptAndConfigure() {
		configureSocketReceiveBufferSize();
		configureSocketSendBufferSize();
		configureKeepAlivePacketsMode();
		configureSmallerPacketsSendingPolicy();
		configureSocketTimeout();
		try {
			return this.connectionFactory.createConnection(clientSocket);
		} catch (IOException e) {
			logger.error("Cannot create new HttpSeverConnection", e);
			throw new IllegalStateException(e);
		}
	}

	private void configureSocketTimeout() {
		socketConfiguration.getSocketTimeoutInMiliseconds().map(new OptionalMapper<Integer>() {
			public void present(Integer timeoutInMiliseconds) {
				try {
					clientSocket.setSoTimeout(timeoutInMiliseconds);
				} catch (SocketException e) {
					logger.error("There was an error setting socket timeout",e);
					throw new IllegalStateException(e);
				}
			}
		});
	}

	private void configureSocketReceiveBufferSize() {
		socketConfiguration.getSocketReceiveBufferSizeInBytes().map(new OptionalMapper<Integer>() {
			public void present(Integer size) {
				try {
					clientSocket.setReceiveBufferSize(size);
				} catch (SocketException e) {
					logger.error("There was an error setting socket receive buffer size", e);
					throw new IllegalStateException(e);
				}
			}
		});
	}

	private void configureSocketSendBufferSize() {
		socketConfiguration.getSocketSendBufferSizeInBytes().map(new OptionalMapper<Integer>() {
			public void present(Integer size) {
				try {
					clientSocket.setSendBufferSize(size);
				} catch (SocketException e) {
					logger.error("There was an error setting socket send buffer size", e);
					throw new IllegalStateException(e);
				}
			}
		});
	}

	private void configureKeepAlivePacketsMode() {
		socketConfiguration.getKeepAlivePacketsMode().map(new OptionalMapper<KeepAliveMode>() {
			public void present(KeepAliveMode mode) {
				try {
					clientSocket.setKeepAlive(mode == ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS);
				} catch (SocketException e) {
					logger.error("There was an error setting socket keep alive mode", e);
					throw new IllegalStateException(e);
				}
			}
		});
	}

	private void configureSmallerPacketsSendingPolicy() {
		socketConfiguration.getSmallerPacketsSendingPolicy().map(new OptionalMapper<SmallerPacketsSendingPolicy>() {
			public void present(SmallerPacketsSendingPolicy policy) {
				try {
					clientSocket.setTcpNoDelay(policy == SEND_SMALLER_PACKETS_INSTANTLY);
				} catch (SocketException e) {
					logger.error("There was an error setting socket TCP no delay flag for smaller packets", e);
					throw new IllegalStateException(e);
				}
			}
		});
	}
}
