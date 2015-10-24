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
import org.apache.http.HttpResponseFactory
import org.apache.http.HttpServerConnection
import org.apache.http.RequestLine
import org.apache.http.protocol.HttpProcessor
import org.apache.http.util.EntityUtils
import org.easymock.EasyMock
import org.junit.Rule
import org.powermock.api.easymock.PowerMock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.commons.Optional.present
import static io.github.kitarek.elasthttpd.model.HttpMethod.DELETE
import static io.github.kitarek.elasthttpd.model.HttpMethod.GET
import static io.github.kitarek.elasthttpd.model.HttpMethod.OPTIONS
import static io.github.kitarek.elasthttpd.model.HttpMethod.POST
import static io.github.kitarek.elasthttpd.model.HttpMethod.PUT
import static org.powermock.api.easymock.PowerMock.mockStatic

@PrepareForTest([EntityUtils.class])
class HttpRequestPrimaryConsumerStaticSpec extends Specification {

	@Rule PowerMockRule powerMockRule = new PowerMockRule();

	@Unroll("Consumer reads #givenHttpMethod request header and finally request body (request entity). Server prepares default HTTP response and send it out via producer")
	def 'Consumer reads request header and finally request body (request entity). Server prepares default HTTP response and send it out via producer'() {
		setup:
			def httpProcessorMock = Mock(HttpProcessor)
			def httpResponseFactoryMock = Mock(HttpResponseFactory)
			def httpConnectionProducer = Mock(HttpConnectionProducer)
			def httpRequestConsumerMock = Mock(HttpRequestConsumer)
			def consumer = new HttpRequestPrimaryConsumer(httpResponseFactoryMock, httpProcessorMock,
					httpConnectionProducer, httpRequestConsumerMock)
			def newConnectionStub = Stub(NewConnection)
			def httpServerConnectionMock = Mock(HttpServerConnection)
		and:
			newConnectionStub.acceptAndConfigure() >> httpServerConnectionMock
		and:
			def entityMock = Mock(HttpEntity)
			def httpEntityRequest = Stub(HttpEntityEnclosingRequest)
			httpEntityRequest.getEntity() >> entityMock
		and:
			def requestLineStub = Stub(RequestLine)
			httpEntityRequest.requestLine >> requestLineStub
			requestLineStub.method >> givenHttpMethod
		and:
			mockStatic(EntityUtils)
			EasyMock.expect(EntityUtils.consume(entityMock)).once();
			PowerMock.replayAll()


		when:
			consumer.consumeConnection(newConnectionStub)


		then:
			1 * httpServerConnectionMock.isOpen() >> true
			1 * httpServerConnectionMock.receiveRequestHeader() >> { httpEntityRequest }
		and: "All the request entities are consumed and won't be read as a part of another request"
			PowerMock.verifyAll()
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
}
