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

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public class HttpConnectionConsumerExecutor implements ConsumerExecutor {

	private ExecutorService manyThreadsExecutor;
	public HttpConnectionConsumerExecutor(int maximumNumberOfAvailableThreads) {
		isTrue(maximumNumberOfAvailableThreads > 0, "Maximum number of available threads must be greater than 0",
				maximumNumberOfAvailableThreads);
		manyThreadsExecutor = newFixedThreadPool(maximumNumberOfAvailableThreads);
	}

	public void execute(final HttpConnectionConsumer consumer, final NewConnection newConnection) {
		manyThreadsExecutor.execute(new Runnable() {
			public void run() {
				consumer.consume(notNull(newConnection, "New connection must be not null!"));
			}
		});
	}
}
