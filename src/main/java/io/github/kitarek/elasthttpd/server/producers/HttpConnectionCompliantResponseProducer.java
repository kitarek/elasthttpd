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

package io.github.kitarek.elasthttpd.server.producers;

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.model.HttpMethod;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.kitarek.elasthttpd.model.HttpMethod.HEAD;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.*;

public class HttpConnectionCompliantResponseProducer implements HttpConnectionProducer {

	public static final Logger logger = LoggerFactory.getLogger(HttpConnectionCompliantResponseProducer.class);
	private final HttpProcessor httpProcessor;

	public HttpConnectionCompliantResponseProducer(HttpProcessor httpProcessor) {
		this.httpProcessor = notNull(httpProcessor, "HTTP procesorr needs to be specified");
	}

	public void sendResponse(HttpServerConnection serverConnection, HttpResponse responseToSend,
							 Optional<HttpMethod> optionalhttpRequestedMethod) {
		notNull(serverConnection, "HTTP server connection cannot be null");
		notNull(responseToSend, "HTTP response to send though connection cannot be null");
		notNull(optionalhttpRequestedMethod, "Optional HTTP request method cannot be null");
		try {
			httpProcessor.process(responseToSend, null);
			sendResponseUnchecked(serverConnection, responseToSend, optionalhttpRequestedMethod);
		} catch (HttpException e) {
			logger.error("There was an HTTP application level exception when trying to send exception", e);
			closeConnectionIfPossible(serverConnection);
		} catch (IOException e) {
			logger.error("I/O exception when trying to send response", e);
			closeConnectionIfPossible(serverConnection);
		}
	}

	private void closeConnectionIfPossible(HttpServerConnection serverConnection) {
		try {
			serverConnection.close();
		} catch (IOException e1) {
			logger.error("Cannot close connection for which HTTP response cannot be send", e1);
		}
	}

	private void sendResponseUnchecked(HttpServerConnection serverConnection, HttpResponse responseToSend, Optional<HttpMethod> optionalhttpRequestedMethod) throws HttpException, IOException {
		serverConnection.sendResponseHeader(responseToSend);
		if (canSendResponseBody(responseToSend, optionalhttpRequestedMethod)) {
				serverConnection.sendResponseEntity(responseToSend);
		}
		serverConnection.flush();
	}

	private boolean canSendResponseBody(HttpResponse responseToSend, Optional<HttpMethod> optionalhttpRequestedMethod) {
		return ((optionalhttpRequestedMethod.isNotPresent() || !isHeadRequestMethod(optionalhttpRequestedMethod)) &&
					getStatusCode(responseToSend) >= SC_OK &&
						getStatusCode(responseToSend) != SC_NO_CONTENT &&
						getStatusCode(responseToSend) != SC_RESET_CONTENT &&
						getStatusCode(responseToSend) != SC_NOT_MODIFIED);
	}

	private boolean isHeadRequestMethod(Optional<HttpMethod> optionalhttpRequestedMethod) {
		return optionalhttpRequestedMethod.isPresent() && optionalhttpRequestedMethod.get() == HEAD;
	}

	private int getStatusCode(HttpResponse responseToSend) {
		return responseToSend.getStatusLine().getStatusCode();
	}
}
