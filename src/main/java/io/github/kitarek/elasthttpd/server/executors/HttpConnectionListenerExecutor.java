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

package io.github.kitarek.elasthttpd.server.executors;

import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener;
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.commons.lang3.Validate.notNull;

// TODO add termination handling and waiting
public class HttpConnectionListenerExecutor implements ListenerExecutor {

	public static final Logger logger = LoggerFactory.getLogger(HttpConnectionListenerExecutor.class);
	private ExecutorService oneThreadExecutor = newSingleThreadExecutor();

	public void execute(final HttpConnectionListener listener, final ListeningSocket socket) {
		oneThreadExecutor.execute(new Runnable() {
			public void run() {
				listener.listenAndPassNewConnections(notNull(socket, "Passed socket cannot be null!"));
			}
		});
	}

	public boolean waitForTermination() {
		try {
			oneThreadExecutor.awaitTermination(0, TimeUnit.DAYS);
			return true;
		} catch (InterruptedException e) {
			logger.error("Awaiting for termination was aborted");
			return false;
		}
	}

	public void terminate() {
		oneThreadExecutor.shutdown();
	}
}
