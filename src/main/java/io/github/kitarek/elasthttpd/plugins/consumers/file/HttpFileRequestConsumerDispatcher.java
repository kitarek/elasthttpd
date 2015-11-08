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

package io.github.kitarek.elasthttpd.plugins.consumers.file;

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.commons.OptionalDispatcher;
import io.github.kitarek.elasthttpd.model.HttpMethod;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequestFactory;
import io.github.kitarek.elasthttpd.plugins.consumers.file.selector.HttpFileRequestConsumerSelector;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED;

class HttpFileRequestConsumerDispatcher implements HttpRequestConsumer {

	private final HttpFileRequestFactory fileRequestFactory;
	private final HttpFileRequestConsumerSelector consumerSelector;

	public HttpFileRequestConsumerDispatcher(HttpFileRequestFactory httpFileRequestFactory,
											 HttpFileRequestConsumerSelector httpFileRequestConsumerSelector) {
		this.fileRequestFactory = notNull(httpFileRequestFactory, "HttpFileRequestFactory instance must be not null");
		this.consumerSelector = notNull(httpFileRequestConsumerSelector,
				"HttpFileRequestConsumerSelector instance must be not null");
	}

	public void consumeRequest(final HttpRequest request, final HttpResponse response) {
		Optional<HttpMethod> httpMethod = HttpMethod.fromString(request.getRequestLine().getMethod());
		httpMethod.dispatch(new HttpMethodOptionalDispatcher(request, response));
	}

	private class HttpMethodOptionalDispatcher implements OptionalDispatcher<HttpMethod> {
		private final HttpResponse response;
		private final HttpRequest request;

		public HttpMethodOptionalDispatcher(HttpRequest request, HttpResponse response) {
			this.response = response;
			this.request = request;
		}

		public void notPresent() {
			respondWithMethodNotAllowed(response);
		}

		public void present(HttpMethod httpMethod) {
			Optional<HttpFileRequestConsumer> optionalConsumer = consumerSelector.selectConsumer(httpMethod);
			optionalConsumer.dispatch(new HttpFileRequestConsumerOptionalDispatcher());
		}

		private class HttpFileRequestConsumerOptionalDispatcher implements OptionalDispatcher<HttpFileRequestConsumer> {
			public void notPresent() {
				respondWithMethodNotImplemented(response);
			}

			public void present(final HttpFileRequestConsumer consumer) {
				delegateToConsumer(consumer, request, response);
			}
		}
	}

	private void delegateToConsumer(HttpFileRequestConsumer consumer, HttpRequest request, HttpResponse response) {
		HttpFileRequest fileRequest = fileRequestFactory.createNew(request, response);
		consumer.consumeFileRequest(fileRequest);
	}

	private void respondWithMethodNotImplemented(HttpResponse response) {
		setResponseCodeAndReason(response, SC_NOT_IMPLEMENTED, "Method not implemented");
	}

	private void respondWithMethodNotAllowed(HttpResponse response) {
		setResponseCodeAndReason(response, HttpStatus.SC_METHOD_NOT_ALLOWED, "Method not allowed");
	}

	private void setResponseCodeAndReason(HttpResponse response, int scNotImplemented, String reason) {
		response.setStatusCode(scNotImplemented);
		response.setReasonPhrase(reason);
	}

}
