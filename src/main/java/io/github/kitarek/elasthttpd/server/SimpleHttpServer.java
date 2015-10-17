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

package io.github.kitarek.elasthttpd.server;


import io.github.kitarek.elasthttpd.server.executors.ListenerExecutor;
import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener;
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket;

import static org.apache.commons.lang3.Validate.notNull;

public class SimpleHttpServer implements HttpServer {

	private final ListenerExecutor executor;
	private final HttpConnectionListener listener;
	private final ListeningSocket socket;

	public SimpleHttpServer(ListenerExecutor executor, HttpConnectionListener listener, ListeningSocket socket) {
		this.executor = notNull(executor, "Listener executor must be not null");;
		this.listener = notNull(listener, "HTTP connection listener must be not null");
		this.socket = notNull(socket, "Listening socket must be not null");
	}

	public void start() {
		executor.execute(listener, socket);
	}
}
