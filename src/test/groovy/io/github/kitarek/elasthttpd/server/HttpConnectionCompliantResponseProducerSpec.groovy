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

package io.github.kitarek.elasthttpd.server
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionCompliantResponseProducer
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionProducer
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpServerConnection
import org.apache.http.RequestLine
import org.apache.http.StatusLine
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpProcessor
import spock.lang.Specification
import spock.lang.Unroll

import static org.apache.http.HttpStatus.SC_ACCEPTED
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_RESET_CONTENT
import static org.apache.http.protocol.HttpCoreContext.HTTP_CONNECTION
import static org.apache.http.protocol.HttpCoreContext.HTTP_REQUEST

class HttpConnectionCompliantResponseProducerSpec extends Specification {

	def httpResponseStub
	def statusLineStub
	def httpServerConnection
	def HttpConnectionProducer httpConnnectionProducer
	def HttpProcessor httpProcessorMock


	def setup() {
		httpResponseStub = Stub(HttpResponse)
		statusLineStub = Stub(StatusLine)
		httpProcessorMock = Mock(HttpProcessor)
		httpServerConnection = Mock(HttpServerConnection)
		httpConnnectionProducer = new HttpConnectionCompliantResponseProducer(httpProcessorMock);
	}

	def 'HTTP connection producer cannot be created without HTTP processor'() {
		when:
			new HttpConnectionCompliantResponseProducer(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll
	def 'HTTP connection producer will faill to send response header if HTTP response and context are not defined (null)'() {
		when:
			httpConnnectionProducer.sendResponse(responseToSend, httpContext)

		then:
			thrown(NullPointerException)

		where:
			httpContext                | responseToSend
			Mock(HttpContext)          | null
			null                       | null
			null                       | Mock(HttpResponse)
	}

	@Unroll
	def 'HTTP connection producer will faill to send response header if HTTP server connection, HTTP request or optional HTTP request method is not defined (null)'() {
		given:
			def HttpContext httpContextStub = Stub(HttpContext)
			httpContextStub.getAttribute(HTTP_CONNECTION) >> serverConnection
			httpContextStub.getAttribute(HTTP_REQUEST) >> request

		when:
			httpConnnectionProducer.sendResponse(Mock(HttpResponse), httpContextStub)

		then:
			thrown(NullPointerException)

		where:
			serverConnection           | request
		    Mock(HttpServerConnection) | null
			null                       | null
			null                       | Mock(HttpRequest)
	}

	@Unroll
	def 'HTTP connection producer will always send response header and only response entity when there is no HEAD request method or NO_CONTENT/NOT_MODIFIED/RESET_CONTENT status code'() {
		given:
			def httpRequestStub = Stub(HttpRequest)
			def requestLineStub = Stub(RequestLine)
			def HttpContext httpContextStub = Stub(HttpContext)
			httpContextStub.getAttribute(HTTP_CONNECTION) >> httpServerConnection
			httpContextStub.getAttribute(HTTP_REQUEST) >> httpRequestStub
		and:
			httpRequestStub.getRequestLine() >> requestLineStub
			requestLineStub.method >> givenHttpMethod
		and:
			httpResponseStub.getStatusLine() >> statusLineStub
			statusLineStub.statusCode >> givenStatusCode

		when:
			httpConnnectionProducer.sendResponse(httpResponseStub, httpContextStub)

		then:
			1 * httpProcessorMock.process(httpResponseStub, _)
			1 * httpServerConnection.sendResponseHeader(httpResponseStub)
			sendResponseEntityCalls * httpServerConnection.sendResponseEntity(httpResponseStub)
			1 * httpServerConnection.flush()
			0 * _

		where:
			givenStatusCode  | givenHttpMethod | sendResponseEntityCalls
			SC_NO_CONTENT    | null            | 0
			SC_NO_CONTENT    | "HEAD"          | 0
			SC_OK            | "HEAD"          | 0
			SC_OK            | "GET"           | 1
			SC_OK            | "POST"          | 1
			SC_NOT_MODIFIED  | "PUT"           | 0
			SC_RESET_CONTENT | "DELETE"        | 0
			SC_ACCEPTED      | "POST"          | 1
	}
}

