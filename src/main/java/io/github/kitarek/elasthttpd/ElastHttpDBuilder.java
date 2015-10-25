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
 * TODO javadoc
 */
public interface ElastHttpDBuilder {

	int DEFAULT_MAXIMUM_NUMBER_OF_CONCURRENT_CONNECTIONS = 10;
	String DEFAULT_SERVER_INFO_LINE = "ElastHttpD";

	ElastHttpDBuilder serverInfo(String serverInfoLine);
	ElastHttpDBuilder networkConfiguration(NetworkConfigurationBuilder preconfiguredBuilder);
	ElastHttpDBuilder customRequestConsumer(HttpRequestConsumer customhttpRequestConsumer);
	ElastHttpDBuilder concurrentConnections(int maximumNumberOfThreads);
	HttpServer createAndReturn();

	void run();
	void runAsync();
}
