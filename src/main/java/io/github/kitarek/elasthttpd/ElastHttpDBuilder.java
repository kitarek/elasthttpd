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

package io.github.kitarek.elasthttpd;

import io.github.kitarek.elasthttpd.server.HttpServer;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;
import io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder;

/**
 * The flexible HTTP server builder API
 */
public interface ElastHttpDBuilder {

	int DEFAULT_MAXIMUM_NUMBER_OF_CONCURRENT_CONNECTIONS = 10;
	String DEFAULT_SERVER_INFO_LINE = "ElastHttpD";

	/**
	 * Change preconfigured HTTP server signature that is added to each handled response.
	 * @param serverInfoLine the not null string representing server signature
	 * @return the current builder for chaining other methods
	 */
	ElastHttpDBuilder serverInfo(String serverInfoLine);

	/**
	 * Reconfigure network settings for HTTP server according to {@link NetworkConfigurationBuilder} class instance
	 * with already overriden changes
	 *
	 * @param preconfiguredBuilder the instance of builder that is configured and ready to be passed.
	 * @return the current builder for chaining other methods
	 */
	ElastHttpDBuilder networkConfiguration(NetworkConfigurationBuilder preconfiguredBuilder);

	/**
	 * Overrides default request consumer that responds always in the same way and allows for programming almost
	 * all different request/response combination for the whole webserver.
	 *
	 * The passed class will be the only one single consumer of all HTTP server requests.
	 * You may want to delegate some requests to other custom request consumers creating so called composite
	 * consumer and passing it here.
	 *
	 * @param customhttpRequestConsumer the single consumer instance that will handle all valid requests.
	 * @return the current builder for chaining other methods
	 */
	ElastHttpDBuilder customRequestConsumer(HttpRequestConsumer customhttpRequestConsumer);

	/**
	 * Allows to configure number of threads and connections that can run in parallel different requests. Please
	 * note that for {@code value > 1} consumer passed here: {@link #customRequestConsumer(HttpRequestConsumer)}
	 * needs to be thread safe.
	 *
	 * @param maximumNumberOfThreads the number of parallel connections to be handled by server
	 * @return the current builder for chaining other methods
	 */
	ElastHttpDBuilder concurrentConnections(int maximumNumberOfThreads);

	/**
	 * Create HTTP server with the builder current state of configuration
	 * @return An instance to HTTP server that is not running yet.
	 */
	HttpServer createAndReturn();

	/**
	 * Create and run the configured server and blocks the caller thread waiting until server won't be stopped
	 * by another thread
	 */
	void run();

	/**
	 * Run server asynchronously in the background thread. Use this call if you need the caller thread and you
	 * don't want to stop or wait for server.
	 */
	void runAsync();
}
