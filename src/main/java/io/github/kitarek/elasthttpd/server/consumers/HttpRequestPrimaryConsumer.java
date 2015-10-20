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

package io.github.kitarek.elasthttpd.server.consumers;

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.model.HttpMethod;
import io.github.kitarek.elasthttpd.server.networking.NewConnection;
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionProducer;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.*;
import static org.apache.http.util.EncodingUtils.getAsciiBytes;

/**
 * Handles and validates new HTTP connection and its requests at the very begining of processing. Performs only
 * basic processing for new HTTP request
 */
public class HttpRequestPrimaryConsumer implements HttpConnectionConsumer {
	public static final Logger logger = LoggerFactory.getLogger(HttpRequestPrimaryConsumer.class);
	public static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 1);
	public static final ContentType PLAIN_DEFAULT_CONTENT_TYPE = ContentType.create("text/plain", "US-ASCII");

	private final HttpResponseFactory httpResponseFactory; //DefaultHttpResponseFactory.INSTANCE;
	private final HttpProcessor httpProcessor;
	private final HttpConnectionProducer httpConnectionProducer;

	public HttpRequestPrimaryConsumer(HttpResponseFactory httpResponseFactory,
									  HttpProcessor httpProcessor,
									  HttpConnectionProducer httpConnectionProducer) {
		this.httpResponseFactory = notNull(httpResponseFactory, "HTTP Response factory cannot be null");
		this.httpProcessor = notNull(httpProcessor, "HTTP Processor cannot be null");
		this.httpConnectionProducer = notNull(httpConnectionProducer, "HTTP Connection Producer cannot be null");
	}

	public void consumeConnection(NewConnection c) {
		final HttpServerConnection connection = c.acceptAndConfigure();
		consumeRequestsUntilConnectionIsOpen(connection);
		flushConnection(connection);
		closeTheConnection(connection);
	}

	private void consumeRequestsUntilConnectionIsOpen(HttpServerConnection connection) {
		while (connection.isOpen()) {
			consumeSingleRequest(connection);
		}
	}

	private void flushConnection(HttpServerConnection connection) {
		try {
			connection.flush();
		} catch (IOException e) {
			logger.warn("There was a non-critical error flushing the connection stream", e);
		}
	}

	private void closeTheConnection(HttpServerConnection connection) {
		try {
			connection.close();
		} catch (IOException e) {
			logger.warn("There was a non-critical error closing the connection stream", e);
		}
	}


	private void consumeSingleRequest(HttpServerConnection connection) {
		try {
			final HttpRequest request = connection.receiveRequestHeader();
//			request.
		} catch (HttpException e) {
			HttpResponse httpResponse = respondToHttpProtocolLevelException(e);
			httpConnectionProducer.sendResponse(connection, httpResponse, Optional.<HttpMethod>empty());
		} catch (IOException e) {
			logger.error("There was an I/O level error receiving request header. Cannot continue with current request");
		}
	}

	private HttpResponse respondToHttpProtocolLevelException(HttpException e) {
		final HttpResponse httpResponse = httpResponseFactory.newHttpResponse(PROTOCOL_VERSION, getHttpStatusFromException(e), null);
		fillEntityOfHttpResponseWithExceptionMessage(httpResponse, e.getMessage());
		logger.error("There was a HTTP level error receiving request header. Responding with: {}", httpResponse.getStatusLine());
		return httpResponse;
	}

	private void fillEntityOfHttpResponseWithExceptionMessage(final HttpResponse httpResponse, final String message) {
		if (message != null) {
			httpResponse.setEntity(new ByteArrayEntity(getAsciiBytes(message), PLAIN_DEFAULT_CONTENT_TYPE));
		}
	}

	private int getHttpStatusFromException(HttpException e) {
		return (e instanceof MethodNotSupportedException) ? SC_METHOD_NOT_ALLOWED :
				(e instanceof UnsupportedHttpVersionException) ? SC_HTTP_VERSION_NOT_SUPPORTED :
				(e instanceof ProtocolException) ? SC_BAD_REQUEST :
				SC_INTERNAL_SERVER_ERROR;
	}


}
