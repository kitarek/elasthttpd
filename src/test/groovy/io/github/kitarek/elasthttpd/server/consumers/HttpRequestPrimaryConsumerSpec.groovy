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

package io.github.kitarek.elasthttpd.server.consumers
import io.github.kitarek.elasthttpd.server.networking.NewConnection
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionProducer
import org.apache.http.HttpEntity
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseFactory
import org.apache.http.HttpServerConnection
import org.apache.http.MethodNotSupportedException
import org.apache.http.ProtocolException
import org.apache.http.ProtocolVersion
import org.apache.http.RequestLine
import org.apache.http.UnsupportedHttpVersionException
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpProcessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.commons.Optional.present
import static io.github.kitarek.elasthttpd.model.HttpMethod.DELETE
import static io.github.kitarek.elasthttpd.model.HttpMethod.GET
import static io.github.kitarek.elasthttpd.model.HttpMethod.HEAD
import static io.github.kitarek.elasthttpd.model.HttpMethod.OPTIONS
import static io.github.kitarek.elasthttpd.model.HttpMethod.POST
import static io.github.kitarek.elasthttpd.model.HttpMethod.PUT
import static org.apache.commons.io.IOUtils.toString
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CONTINUE
import static org.apache.http.HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.util.EncodingUtils.getAsciiBytes

class HttpRequestPrimaryConsumerSpec extends Specification {

	def httpProcessorMock
	def HttpServerConnection httpServerConnectionMock
	def HttpResponseFactory httpResponseFactoryMock
	def HttpConnectionProducer httpConnectionProducer
	def HttpRequestConsumer httpRequestConsumerMock
	def consumer
	def NewConnection newConnectionMock
	def NewConnection newConnectionStub


	def setup() {
		httpProcessorMock = Mock(HttpProcessor)
		httpResponseFactoryMock = Mock(HttpResponseFactory)
		httpConnectionProducer = Mock(HttpConnectionProducer)
		httpRequestConsumerMock = Mock(HttpRequestConsumer)
		consumer = new HttpRequestPrimaryConsumer(httpResponseFactoryMock, httpProcessorMock, httpConnectionProducer,
				httpRequestConsumerMock)
		newConnectionMock = Mock(NewConnection)
		newConnectionStub = Stub(NewConnection)
		httpServerConnectionMock = Mock(HttpServerConnection)
	}

	@Unroll
	def 'The HttpRequestPrimaryConsuner cannot be initialized without correct instance of HttpProcessor and HttpResponseFactory'() {
		when:
			new HttpRequestPrimaryConsumer(httpResponseFactory, httpProcessor, httpConnectionProducer, httpRequestConsumer)

		then:
			thrown(NullPointerException)

		where:
			httpResponseFactory       | httpProcessor        | httpConnectionProducer       | httpRequestConsumer
			Mock(HttpResponseFactory) | null                 | null                         | null
			null                      | Mock(HttpProcessor)  | null                         | null
			null                      | null                 | null                         | null
			Mock(HttpResponseFactory) | null                 | Mock(HttpConnectionProducer) | null
			null                      | Mock(HttpProcessor)  | Mock(HttpConnectionProducer) | null
			null                      | null                 | Mock(HttpConnectionProducer) | null
			null                      | null                 | null                         | null
			Mock(HttpResponseFactory) | null                 | null                         | Mock(HttpRequestConsumer)
			null                      | Mock(HttpProcessor)  | null                         | Mock(HttpRequestConsumer)
			null                      | null                 | null                         | Mock(HttpRequestConsumer)
			Mock(HttpResponseFactory) | null                 | Mock(HttpConnectionProducer) | Mock(HttpRequestConsumer)
			null                      | Mock(HttpProcessor)  | Mock(HttpConnectionProducer) | Mock(HttpRequestConsumer)
			null                      | null                 | Mock(HttpConnectionProducer) | Mock(HttpRequestConsumer)
			null                      | null                 | null                         | Mock(HttpRequestConsumer)
	}

	def 'The new connection is accepted and configured in scope of work of HttpRequestPrimaryConsumer'() {
		when:
			consumer.consumeConnection(newConnectionMock)

		then:
			1 * newConnectionMock.acceptAndConfigure() >> httpServerConnectionMock
	}


	def 'The new connection needs to be correctly flushed and closed at the very end of primary consumer if its open'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.isOpen() >> false
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.flush()
			1 * httpServerConnectionMock.close()
	}

	def 'The first thing we need to do is to check if newly opened connection is still open and valid'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			2 * httpServerConnectionMock.isOpen() >> false
	}

	def 'If the connection is still open we should try to read request header first'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
			def httpRequestStub = Stub(HttpRequest)

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> httpRequestStub
			2 * httpServerConnectionMock.isOpen() >> false
	}

	@Unroll
	def 'If the connection is still open and there was a HTTP application exception when reading request header we should return appropriate response to client'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
			def expectedProtocolVersion = new ProtocolVersion("HTTP", 1, 1)
			def httpResponseStub = Stub(HttpResponse)
			def capturedEntity
		and:
			httpResponseStub.setEntity(_) >> { parameters ->
				capturedEntity = parameters[0]
			}

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> { throw exception }
			2 * httpServerConnectionMock.isOpen() >> false
		and:
			1 * httpResponseFactoryMock.newHttpResponse(expectedProtocolVersion, expectedStatus, _) >> httpResponseStub
			0 * httpResponseFactoryMock.newHttpResponse(_, _, _)
		and:
			areEntitiesEqual(capturedEntity, expectedEntity)
		and:
			1 * httpConnectionProducer.sendResponse(httpResponseStub, { it instanceof HttpContext && it != null })

		where:
			exception                                     | expectedStatus                | expectedEntity
			new HttpException()                           | SC_INTERNAL_SERVER_ERROR      | null
			new HttpException("Test")                     | SC_INTERNAL_SERVER_ERROR      | httpEntityForSimpleUsAsciiMessage("Test")
			new MethodNotSupportedException(null)         | SC_METHOD_NOT_ALLOWED         | null
			new MethodNotSupportedException("Test2")      | SC_METHOD_NOT_ALLOWED         | httpEntityForSimpleUsAsciiMessage("Test2")
			new UnsupportedHttpVersionException()         | SC_HTTP_VERSION_NOT_SUPPORTED | null
			new UnsupportedHttpVersionException("Test3")  | SC_HTTP_VERSION_NOT_SUPPORTED | httpEntityForSimpleUsAsciiMessage("Test3")
			new ProtocolException()                       | SC_BAD_REQUEST                | null
			new ProtocolException("Test4")                | SC_BAD_REQUEST                | httpEntityForSimpleUsAsciiMessage("Test4")
	}

	@Unroll
	def 'Consumer reads request header and checks if client ask server that it may continue sending request body. Client is notified positively via producer and consumer tries to receive that request body'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
			def expectedProtocolVersion = new ProtocolVersion("HTTP", 1, 1)
			def httpResponseStub = Stub(HttpResponse)
			def capturedEntity
		and:
			def httpEntityRequest = Stub(HttpEntityEnclosingRequest)
			httpEntityRequest.expectContinue() >> true
		and:
			def requestLineStub = Stub(RequestLine)
			httpEntityRequest.requestLine >> requestLineStub
			requestLineStub.method >> givenHttpMethod
		and:
			httpResponseStub.setEntity(_) >> { parameters ->
				capturedEntity = parameters[0]
			}


		when:
			consumer.consumeConnection(newConnectionStub)


		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> { httpEntityRequest }
		and:
			1 * httpServerConnectionMock.isOpen() >> false
		and:
			1 * httpResponseFactoryMock.newHttpResponse(expectedProtocolVersion, expectedStatus, _) >> httpResponseStub
		and: "the request should be always accepted by sending only response header without entity"
			capturedEntity == null
		and:
			1 * httpConnectionProducer.sendResponse(httpResponseStub, { it instanceof HttpContext && it != null })
		and:
			1 * httpServerConnectionMock.receiveRequestEntity(httpEntityRequest)
		and:
			1 * httpServerConnectionMock.isOpen() >> false

		where:
			givenHttpMethod | expectedOptionalHttpMethod | expectedStatus
			"GET"           | present(GET)               | SC_CONTINUE
			"HEAD"          | present(HEAD)              | SC_CONTINUE
			"PUT"           | present(PUT)               | SC_CONTINUE
			"POST"          | present(POST)              | SC_CONTINUE
	}

	def 'Consumer reads request header and checks if client ask server that it may continue sending request body. Server tries to receive that request body immediately'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
		and:
			def httpEntityRequest = Stub(HttpEntityEnclosingRequest)
			httpEntityRequest.expectContinue() >> false

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> { httpEntityRequest }
		and:
			2 * httpServerConnectionMock.isOpen() >> false
		and:
			1 * httpServerConnectionMock.receiveRequestEntity(httpEntityRequest)
	}

	@Unroll("Consumer reads #givenHttpMethod request header and finally request body (request entity). Server prepares default HTTP response and send it out via producer")
	def 'Consumer reads request header and finally request body (request entity). Server prepares default HTTP response and send it out via producer'() {
		setup:

		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
			def expectedProtocolVersion = new ProtocolVersion("HTTP", 1, 1)
			def httpResponseStub = Stub(HttpResponse)
			def capturedEntity
		and:
			def entityMock = Mock(HttpEntity)
			def httpEntityRequest = Stub(HttpEntityEnclosingRequest)
			httpEntityRequest.expectContinue() >> false
			httpEntityRequest.getEntity() >> entityMock
		and:
			httpResponseStub.setEntity(_) >> { parameters ->
				capturedEntity = parameters[0]
			}

		and:
			def requestLineStub = Stub(RequestLine)
			httpEntityRequest.requestLine >> requestLineStub
			requestLineStub.method >> givenHttpMethod


		when:
			consumer.consumeConnection(newConnectionStub)


		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> { httpEntityRequest }
		and:
			1 * httpServerConnectionMock.receiveRequestEntity(httpEntityRequest)
		and: "The default response is created"
			1 * httpResponseFactoryMock.newHttpResponse(expectedProtocolVersion, SC_OK, _) >> httpResponseStub
			0 * httpResponseFactoryMock.newHttpResponse(_, _, _)
		and: "The HTTP request is correctly preprocessed by processor"
			1 * httpProcessorMock.process(httpEntityRequest, null);
		and: "The consumption of the fully prepared request and response object is delegated to dedicated consumer"
			1 * httpRequestConsumerMock.consumeRequest(httpEntityRequest, httpResponseStub)
		and: "The possibly updated default response is sent to client"
			1 * httpConnectionProducer.sendResponse(httpResponseStub, { it instanceof HttpContext && it != null })
			0 * httpConnectionProducer._
		and: "The connection is closed by client or server when it's not persistent"
			2 * httpServerConnectionMock.isOpen() >> false

		where:
			givenHttpMethod | expectedOptionalHttpMethod
			"GET"           | present(GET)
			"POST"          | present(POST)
			"PUT"           | present(PUT)
			"DELETE"        | present(DELETE)
			"OPTIONS"       | present(OPTIONS)
	}

	// Helper method needed as equals() is not overridden for ByteArrayEntity
	def areEntitiesEqual(ByteArrayEntity byteArrayEntity, ByteArrayEntity expectedByteArrayEntity) {
		(byteArrayEntity == null && expectedByteArrayEntity  == null ||
				byteArrayEntity != null && expectedByteArrayEntity != null &&
				byteArrayEntity.getContentLength() == expectedByteArrayEntity.getContentLength() &&
				byteArrayEntity.getContentType() == byteArrayEntity.getContentType() &&
				toString(byteArrayEntity.getContent(), "UTF-8") == toString(expectedByteArrayEntity.getContent(), "UTF-8"))
	}

	@Shared
	private def httpEntityForSimpleUsAsciiMessage = { message ->
		new ByteArrayEntity(getAsciiBytes(message), ContentType.create("text/plain", "US-ASCII"))
	}

}
