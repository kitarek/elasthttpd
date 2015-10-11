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

package io.github.kitarek.elasthttpd.server.core;


import io.github.kitarek.elasthttpd.story.OneResponseStory;
import org.apache.http.ExceptionLogger;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// TODO AK unit test coverage
public class HttpCoreBasedServer implements io.github.kitarek.elasthttpd.server.HttpServer {

	public Logger logger = LoggerFactory.getLogger(HttpCoreBasedServer.class);

	public void start() {
		SocketConfig socketConfig = prepareSocket();
		final HttpServer server = configureHttpServer(socketConfig);
		startServerAndBlockCurrentThread(server);
	}

	private void startServerAndBlockCurrentThread(HttpServer server) {
		try {
			server.start();
			server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (IOException e) {
			logger.error("A fatal exception occured", e);
		} catch (InterruptedException e) {
			logger.info("Server runtime was interruppted. Aborting");
		}
	}

	private HttpServer configureHttpServer(SocketConfig socketConfig) {
		return ServerBootstrap.bootstrap()
					.setListenerPort(8080)
					.setServerInfo("Test/1.1")
					.setSocketConfig(socketConfig)
					.setExceptionLogger(new StdErrorExceptionLogger())
					.registerHandler("*", new OneResponseStory())
					.create();
	}

	private SocketConfig prepareSocket() {
		return SocketConfig.custom()
					.setSoTimeout(15000)
					.setTcpNoDelay(true)
					.build();
	}

	private class StdErrorExceptionLogger implements ExceptionLogger {
		public void log(Exception ex) {
			logger.error("An exception occured during handling request", ex);
		}
	}
}
