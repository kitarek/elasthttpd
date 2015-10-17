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


import io.github.kitarek.elasthttpd.server.consumers.HttpConnectionConsumer;
import io.github.kitarek.elasthttpd.server.networking.NewConnection;

/**
 * Manage the execution and environment of {@link HttpConnectionConsumer} outside of caller thread and runtime
 * environment.
 *
 * Allows for independent run of each new connection so each can be serviced at the same time.
 *
 * This benefits i.e. in separate and independent exception handling/throwing beyond the caller of this method.
 */
public interface ConsumerExecutor {
	/**
	 * Execute {@link HttpConnectionConsumer} asynchronously
	 *
	 * @param consumer HTTP connection consumer that will be run and then working asynchronously from the caller thread
	 * @param newConnection the new connection will be the exclusive resource that cannot be shared and used within any
	 *                         other thread
	 */
	void execute(HttpConnectionConsumer consumer, NewConnection newConnection);
}
