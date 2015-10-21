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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.kitarek.elasthttpd.model.HttpMethod.fromString;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.*;
import static org.apache.http.util.EncodingUtils.getAsciiBytes;

/**
 * Handles and validates new HTTP connection and its requests at the very begining of processing. Performs only
 * basic processing for new HTTP request
 */
public class HttpRequestPrimaryConsumer implements HttpConnectionConsumer {
	public static final Logger logger = LoggerFactory.getLogger(HttpRequestPrimaryConsumer.class);
	public static final ProtocolVersion DEFAULT_PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 1);
	public static final ContentType PLAIN_DEFAULT_CONTENT_TYPE = ContentType.create("text/plain", "US-ASCII");

	private final HttpResponseFactory httpResponseFactory; //DefaultHttpResponseFactory.INSTANCE;
	private final HttpProcessor httpProcessor;
	private final HttpConnectionProducer httpConnectionProducer;
	private final HttpRequestConsumer httpRequestConsumer;

	public HttpRequestPrimaryConsumer(HttpResponseFactory httpResponseFactory,
									  HttpProcessor httpProcessor,
									  HttpConnectionProducer httpConnectionProducer,
									  HttpRequestConsumer httpRequestConsumer) {
		this.httpResponseFactory = notNull(httpResponseFactory, "HTTP Response factory cannot be null");
		this.httpProcessor = notNull(httpProcessor, "HTTP Processor cannot be null");
		this.httpConnectionProducer = notNull(httpConnectionProducer, "HTTP Connection Producer cannot be null");
		this.httpRequestConsumer = notNull(httpRequestConsumer, "HTTP Request consumer cannot be null");
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
			consumeSingleRequestUnchecked(connection);
		} catch (HttpException e) {
			HttpResponse httpResponse = respondToHttpProtocolLevelException(e);
			httpConnectionProducer.sendResponse(connection, httpResponse, Optional.<HttpMethod>empty());
		} catch (IOException e) {
			logger.error("There was an I/O level error receiving request header. Cannot continue with current request");
		}
		// TODO handle here keep alive
	}

	private HttpResponse respondToHttpProtocolLevelException(Exception e) {
		final HttpResponse httpResponse = httpResponseFactory.newHttpResponse(DEFAULT_PROTOCOL_VERSION, getHttpStatusFromException(e), null);
		fillEntityOfHttpResponseWithExceptionMessage(httpResponse, e.getMessage());
		logger.error("There was a HTTP level error receiving request header. Responding with: {}", httpResponse.getStatusLine());
		return httpResponse;
	}

	private void fillEntityOfHttpResponseWithExceptionMessage(final HttpResponse httpResponse, final String message) {
		if (message != null) {
			httpResponse.setEntity(new ByteArrayEntity(getAsciiBytes(message), PLAIN_DEFAULT_CONTENT_TYPE));
		}
	}

	private int getHttpStatusFromException(Exception e) {
		return (e instanceof MethodNotSupportedException) ? SC_METHOD_NOT_ALLOWED :
				(e instanceof UnsupportedHttpVersionException) ? SC_HTTP_VERSION_NOT_SUPPORTED :
				(e instanceof ProtocolException) ? SC_BAD_REQUEST :
				SC_INTERNAL_SERVER_ERROR;
	}

	private void consumeSingleRequestUnchecked(HttpServerConnection connection) throws HttpException, IOException {
		final HttpRequest request = connection.receiveRequestHeader();
		fetchRequestEntity(request, connection);
		final HttpResponse response = doProcessRequestAndPrepareResponse(request);
		consumeFullyReuqestBody(connection, request);
		httpConnectionProducer.sendResponse(connection, response, getHttpMethodFromRequest(request));
	}

	private void fetchRequestEntity(HttpRequest request, HttpServerConnection connection) throws IOException, HttpException {
		if (isRequestImplementingEntity(request)) {
			HttpEntityEnclosingRequest requestWithEntity = upgradeHttpRequestSupportingEntities(request);
			if (clientAsksForConfirmationToContinueTransmission(requestWithEntity)) {
				confirmContinuationToClientBySendingContinueResponse(connection, request);
			}
			connection.receiveRequestEntity(requestWithEntity);
		}
	}

	private boolean clientAsksForConfirmationToContinueTransmission(HttpEntityEnclosingRequest requestWithEntity) {
		return requestWithEntity.expectContinue();
	}

	private void confirmContinuationToClientBySendingContinueResponse(HttpServerConnection connection, HttpRequest request) {
		final HttpResponse responseToSend = httpResponseFactory.newHttpResponse(DEFAULT_PROTOCOL_VERSION, SC_CONTINUE, null);
		final Optional<HttpMethod> optionalhttpRequestedMethod = getHttpMethodFromRequest(request);
		httpConnectionProducer.sendResponse(connection,	responseToSend,	optionalhttpRequestedMethod);
	}

	private Optional<HttpMethod> getHttpMethodFromRequest(HttpRequest request) {
		return fromString(request.getRequestLine().getMethod());
	}

	private void consumeFullyReuqestBody(HttpServerConnection connection, HttpRequest request) throws IOException {
		if (isRequestImplementingEntity(request)) {
			final HttpEntityEnclosingRequest entityEnclosingRequest = upgradeHttpRequestSupportingEntities(request);
			final HttpEntity entity = entityEnclosingRequest.getEntity();
			EntityUtils.consume(entity);
		}
	}

	private boolean isRequestImplementingEntity(HttpRequest request) {
		return (request instanceof HttpEntityEnclosingRequest);
	}

	private HttpEntityEnclosingRequest upgradeHttpRequestSupportingEntities(HttpRequest request) {
		return (HttpEntityEnclosingRequest) request;
	}

	private HttpResponse doProcessRequestAndPrepareResponse(HttpRequest request) throws IOException, HttpException {
		final HttpResponse response = httpResponseFactory.newHttpResponse(DEFAULT_PROTOCOL_VERSION, SC_OK, null);
		httpProcessor.process(request, null);
		try {
			httpRequestConsumer.consumeRequest(request, response);
		} catch (RuntimeException e) {
			respondToHttpProtocolLevelException(e);
		}
		return response;
	}


}
