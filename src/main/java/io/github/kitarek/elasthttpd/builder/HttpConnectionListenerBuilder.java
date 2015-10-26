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


import io.github.kitarek.elasthttpd.server.consumers.HttpConnectionConsumer;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestPrimaryConsumer;
import io.github.kitarek.elasthttpd.server.executors.ConsumerExecutor;
import io.github.kitarek.elasthttpd.server.executors.HttpConnectionConsumerExecutor;
import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener;
import io.github.kitarek.elasthttpd.server.listeners.SynchronousStoppableHttpConnectionListener;
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionCompliantResponseProducer;
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionProducer;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.*;

import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

class HttpConnectionListenerBuilder {

	private final String serverInfo;
	private int maximumNumberOfThreads;

	HttpConnectionListenerBuilder(String serverInfo, int maximumNumberOfThreads) {
		this.serverInfo = notNull(serverInfo);
		inclusiveBetween(1, MAX_VALUE, maximumNumberOfThreads);
		this.maximumNumberOfThreads = maximumNumberOfThreads;
	}

	static HttpConnectionListenerBuilder newListener(String serverInfo, int maximumNumberOfThreads) {
		final HttpConnectionListenerBuilder builder = new HttpConnectionListenerBuilder(serverInfo, maximumNumberOfThreads);
		return builder;
	}

	HttpConnectionListener createNow(HttpRequestConsumer httpRequestConsumer) {
		final HttpProcessor httpProcessor = createHttpProcessor();
		final HttpConnectionProducer httpConnectionProducer = new HttpConnectionCompliantResponseProducer(httpProcessor);
		final HttpResponseFactory httpResponseFactory = DefaultHttpResponseFactory.INSTANCE;
		final ConsumerExecutor consumerExecutor = new HttpConnectionConsumerExecutor(this.maximumNumberOfThreads);
		final HttpConnectionConsumer connectionConsumer = new HttpRequestPrimaryConsumer(httpResponseFactory,
				httpProcessor, httpConnectionProducer, httpRequestConsumer);
		final HttpConnectionListener connectionListener =
				new SynchronousStoppableHttpConnectionListener(consumerExecutor, connectionConsumer);
		return connectionListener;
	}

	private HttpProcessor createHttpProcessor() {
		final HttpProcessorBuilder builder = HttpProcessorBuilder.create();
		builder.addAll(new ResponseDate(), new ResponseServer(serverInfo), new ResponseContent(),
				new ResponseConnControl());
		return builder.build();
	}

}
