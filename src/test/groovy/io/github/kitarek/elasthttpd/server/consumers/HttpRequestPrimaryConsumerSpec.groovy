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
import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseFactory
import org.apache.http.HttpServerConnection
import org.apache.http.MethodNotSupportedException
import org.apache.http.ProtocolException
import org.apache.http.ProtocolVersion
import org.apache.http.UnsupportedHttpVersionException
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.protocol.HttpProcessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static org.apache.commons.io.IOUtils.toString
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED
import static org.apache.http.util.EncodingUtils.getAsciiBytes

class HttpRequestPrimaryConsumerSpec extends Specification {

	def consumer
	def NewConnection newConnectionMock
	def NewConnection newConnectionStub
	def HttpServerConnection httpServerConnectionMock
	def HttpResponseFactory httpResponseFactoryMock
	def HttpConnectionProducer httpConnectionProducer


	def setup() {
		def httpProcessorMock = Mock(HttpProcessor)
		httpResponseFactoryMock = Mock(HttpResponseFactory)
		httpConnectionProducer = Mock(HttpConnectionProducer)
		consumer = new HttpRequestPrimaryConsumer(httpResponseFactoryMock, httpProcessorMock, httpConnectionProducer)
		newConnectionMock = Mock(NewConnection)
		newConnectionStub = Stub(NewConnection)
		httpServerConnectionMock = Mock(HttpServerConnection)
	}

	def 'The HttpRequestPrimaryConsuner cannot be initialized without correct instance of HttpProcessor and HttpResponseFactory'() {
		when:
			new HttpRequestPrimaryConsumer(httpResponseFactory, httpProcessor, httpConnectionProducer)

		then:
			thrown(NullPointerException)

		where:
			httpResponseFactory       | httpProcessor        | httpConnectionProducer
			Mock(HttpResponseFactory) | null                 | null
			null                      | Mock(HttpProcessor)  | null
			null                      | null                 | null
			Mock(HttpResponseFactory) | null                 | Mock(HttpConnectionProducer)
			null                      | Mock(HttpProcessor)  | Mock(HttpConnectionProducer)
			null                      | null                 | Mock(HttpConnectionProducer)
			null                      | null                 | null

	}

	def 'The new connection is accepted and configured in scope of work of HttpRequestPrimaryConsumer'() {
		when:
			consumer.consumeConnection(newConnectionMock)

		then:
			1 * newConnectionMock.acceptAndConfigure() >> httpServerConnectionMock
	}


	def 'The new connection needs to be correctly flushed and closed at the very end of primary consumer'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.flush()
			1 * httpServerConnectionMock.close()
	}

	def 'The first thing we need to do is to check if newly opened connection is still open and valid'() {
		given:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock

		when:
			consumer.consumeConnection(newConnectionStub)

		then:
			1 * httpServerConnectionMock.isOpen() >> false
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
			1 * httpServerConnectionMock.isOpen() >> false
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
			1 * httpServerConnectionMock.isOpen() >> false
		and:
			1 * httpResponseFactoryMock.newHttpResponse(expectedProtocolVersion, expectedStatus, null) >> httpResponseStub
			0 * httpResponseFactoryMock.newHttpResponse(_, _, _)
		and:
			areEntitiesEqual(capturedEntity, expectedEntity)
		and:
			1 * httpConnectionProducer.sendResponse(httpServerConnectionMock, httpResponseStub, empty())

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
