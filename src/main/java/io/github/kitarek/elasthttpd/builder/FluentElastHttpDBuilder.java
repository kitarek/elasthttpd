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

package io.github.kitarek.elasthttpd.builder;

import io.github.kitarek.elasthttpd.ElastHttpDBuilder;
import io.github.kitarek.elasthttpd.server.HttpServer;
import io.github.kitarek.elasthttpd.server.SimpleHttpServer;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;
import io.github.kitarek.elasthttpd.server.executors.HttpConnectionListenerExecutor;
import io.github.kitarek.elasthttpd.server.executors.ListenerExecutor;
import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener;
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket;
import io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.kitarek.elasthttpd.builder.HttpConnectionListenerBuilder.newListener;
import static io.github.kitarek.elasthttpd.server.networking.HttpConfiguredServerSocket.newHttpConfiguredServerSocket;
import static io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder.newConfiguration;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.Validate.*;

public class FluentElastHttpDBuilder implements ElastHttpDBuilder {

	public static Logger logger = LoggerFactory.getLogger(FluentElastHttpDBuilder.class);
	private HttpServer server;
	private NetworkConfigurationBuilder networkConfigurationBuilder = newConfiguration();
	private HttpRequestConsumer customHttpRequestConsumer = new DummyHttpRequestConsumer();

	private int maximumNumberOfThreads = DEFAULT_MAXIMUM_NUMBER_OF_CONCURRENT_CONNECTIONS;
	private String serverInfoLine = DEFAULT_SERVER_INFO_LINE;

	public ElastHttpDBuilder serverInfo(String serverInfoLine) {
		this.serverInfoLine = notBlank(serverInfoLine, "Server Information Line cannot be null or blank");
		return this;
	}

	public ElastHttpDBuilder networkConfiguration(NetworkConfigurationBuilder preconfiguredBuilder) {
		networkConfigurationBuilder = notNull(preconfiguredBuilder, "Network configuration builder cannot be null");
		return this;
	}

	public ElastHttpDBuilder customRequestConsumer(HttpRequestConsumer customhttpRequestConsumer) {
		this.customHttpRequestConsumer = notNull(customhttpRequestConsumer, "The custom HTTP request consumer cannot be null");
		return this;
	}

	public ElastHttpDBuilder concurrentConnections(int maximumNumberOfThreads) {
		inclusiveBetween(1, MAX_VALUE, maximumNumberOfThreads, "Maximum number of threads must be greater than 0");
		this.maximumNumberOfThreads = maximumNumberOfThreads;
		return this;
	}

	public HttpServer createAndReturn() {
		final ListenerExecutor listenerExecutor = new HttpConnectionListenerExecutor();
		final ListeningSocket listeningSocket = newHttpConfiguredServerSocket(networkConfigurationBuilder.createNow());
		final HttpConnectionListener connectionListener = newListener(serverInfoLine, maximumNumberOfThreads)
				.createNow(customHttpRequestConsumer);
		server = new SimpleHttpServer(listenerExecutor, connectionListener, listeningSocket);
		return server;
	}

	public void run() {
		runAsync();
		server.waitUntilStopped();
	}

	public void runAsync() {
		createAndReturn();
		server.start();
	}
}
