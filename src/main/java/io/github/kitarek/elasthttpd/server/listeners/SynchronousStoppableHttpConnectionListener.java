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

package io.github.kitarek.elasthttpd.server.listeners;


import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.commons.OptionalMapper;
import io.github.kitarek.elasthttpd.model.ServerState;
import io.github.kitarek.elasthttpd.server.consumers.HttpConnectionConsumer;
import io.github.kitarek.elasthttpd.server.executors.ConsumerExecutor;
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket;
import io.github.kitarek.elasthttpd.server.networking.NewConnection;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.kitarek.elasthttpd.model.ServerState.*;
import static org.apache.commons.lang3.Validate.notNull;

public class SynchronousStoppableHttpConnectionListener implements HttpConnectionListener {

	private final ConsumerExecutor executor;
	private final HttpConnectionConsumer consumer;
	private final AtomicReference<ServerState> state = new AtomicReference<ServerState>(STOPPED);

	public SynchronousStoppableHttpConnectionListener(ConsumerExecutor e, HttpConnectionConsumer c) {
		executor = notNull(e, "Consumer executor need to be defined -- cannot be not null!");
		consumer = notNull(c, "HTTP connection consumer needs to be defined -- cannot be not null!");
	}

	public boolean stopListening() {
		executor.terminate();
		return state.compareAndSet(RUNNING, STOPPING);
	}

	public ServerState getState() {
		return state.get();
	}

	public void listenAndPassNewConnections(ListeningSocket medium) {
		notNull(medium, "Listening socket cannoot be null for listen method");
		if (ensureIfStoppedThenSetToRunning()) {
			listenAndPassConnectionsUntilPossibleAndResetFinallyToStoppedState(medium);
		}
	}

	private boolean ensureIfStoppedThenSetToRunning() {
		return state.compareAndSet(STOPPED, RUNNING);
	}

	private void listenAndPassConnectionsUntilPossibleAndResetFinallyToStoppedState(ListeningSocket medium) {
		try {
			listenForConnectionsAndPassWhenInRunningState(medium);
		} finally {
			state.set(STOPPED);
		}
	}

	private void listenForConnectionsAndPassWhenInRunningState(ListeningSocket medium) {
		while (state.get() == RUNNING) {
			listenForANewConnectionAndDelegateItsProcessingToConsumer(medium);
		}
	}

	private void listenForANewConnectionAndDelegateItsProcessingToConsumer(ListeningSocket medium) {
		Optional<NewConnection> c = medium.listenForANewConnection();
		c.map(new OptionalMapper<NewConnection>() {
			public void present(NewConnection newConnection) {
				executor.execute(consumer, newConnection);
			}
		});
	}

}
