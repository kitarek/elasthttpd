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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode.READ_AND_WRITE;
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder.currentDirectory;
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder.fileServer;
import static io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder.DEFAULT_IPV4_LOOPBACK_LISTEN_ADDRESS;
import static io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder.DEFAULT_LISTEN_PORT;

public class DefaultFullFileHttpServer {
	public static final Logger LOGGER = LoggerFactory.getLogger(DefaultFullFileHttpServer.class);

	public static void main(String[] args) {
		final String rootDirectory = currentDirectory() + "/sandbox";
		LOGGER.info(String.format("Starting webserver with fileServer plugin in FULL mode for: %s listening on http://%s:%s",
				rootDirectory, DEFAULT_IPV4_LOOPBACK_LISTEN_ADDRESS.getHostName(), DEFAULT_LISTEN_PORT));
		ElastHttpD
			.startBuilding()
			.consumeRequestsWithPlugin(
					fileServer().withRootServerDirectory(rootDirectory).allowFileOperations(READ_AND_WRITE)
			)
			.run();
	}

}
