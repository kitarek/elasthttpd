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

package io.github.kitarek.elasthttpd.plugins.consumers.file
import io.github.kitarek.elasthttpd.model.HttpMethod
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumer
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequestFactory
import io.github.kitarek.elasthttpd.plugins.consumers.file.selector.HttpFileRequestConsumerSelector
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.RequestLine
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED

class HttpFileRequestConsumerDispatcherSpec extends Specification {

	@Unroll
	def 'Never cannot create class instance given null constructor arguments'() {
		when:
			new HttpFileRequestConsumerDispatcher(requestFactory, consumerSelector)

		then:
			thrown(NullPointerException)

		where:
			requestFactory  | consumerSelector
			null            | null
			mockedFactory() | null
			null            | mockedSelector()
	}

	def 'Always can create class instance not null constructor dependencies'() {
		given:
			def mockedSelector = mockedSelector()
			def mockedFactory = mockedFactory()

		when:
			new HttpFileRequestConsumerDispatcher(mockedFactory, mockedSelector)

		then:
			notThrown()
	}


	def 'Always pass well-known request method to consumer selector'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Mock(HttpFileRequestConsumerSelector)
		and:
			def HttpRequestConsumer consumer = new HttpFileRequestConsumerDispatcher(factory, selector)
		and:
			def request = Stub(HttpRequest)
			def response = Mock(HttpResponse)
			def requestLine = Stub(RequestLine)
			def httpMethod = HttpMethod.GET
		and:
			def httpMethodId = httpMethod.id
			request.getRequestLine() >> requestLine
			requestLine.getMethod() >> httpMethodId

		when:
			consumer.consumeRequest(request, response)

		then:
			1 * selector.selectConsumer(httpMethod) >> present(Mock(HttpFileRequestConsumer))
			0 * selector._
	}

	def 'Always creates HttpFileRequest if consumer is found'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Stub(HttpFileRequestConsumerSelector)
		and:
			def HttpRequestConsumer consumer = new HttpFileRequestConsumerDispatcher(factory, selector)
		and:
			def request = Stub(HttpRequest)
			def response = Mock(HttpResponse)
			def requestLine = Stub(RequestLine)
			def httpMethod = HttpMethod.GET
		and:
			def httpMethodId = httpMethod.id
			request.requestLine >> requestLine
			requestLine.method >> httpMethodId
		and:
			selector.selectConsumer(_) >> present(Mock(HttpFileRequestConsumer))

		when:
			consumer.consumeRequest(request, response)

		then:
			1 * factory.createNew(request, response) >> Mock(HttpFileRequest)
			0 * factory._
	}

	def 'Always respond with method not implemented response when request method is unknown'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Mock(HttpFileRequestConsumerSelector)
		and:
			def HttpRequestConsumer consumer = new HttpFileRequestConsumerDispatcher(factory, selector)
		and:
			def request = Stub(HttpRequest)
			def response = Mock(HttpResponse)
			def requestLine = Stub(RequestLine)
		and:
			def httpMethodId = "UNKNOWN METHOD"
			request.requestLine >> requestLine
			requestLine.method >> httpMethodId

		when:
			consumer.consumeRequest(request, response)

		then:
			0 * selector._
			0 * factory._
		and:
			1 * response.setStatusCode(SC_METHOD_NOT_ALLOWED)
			1 * response.setReasonPhrase(_)
	}

	def 'Never creates HttpFileRequest if consumer is not found and always respond with method not implemented'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Stub(HttpFileRequestConsumerSelector)
		and:
			def HttpRequestConsumer consumer = new HttpFileRequestConsumerDispatcher(factory, selector)
		and:
			def request = Stub(HttpRequest)
			def response = Mock(HttpResponse)
			def requestLine = Stub(RequestLine)
			def httpMethod = HttpMethod.GET
		and:
			def httpMethodId = httpMethod.id
			request.requestLine >> requestLine
			requestLine.method >> httpMethodId
		and:
			selector.selectConsumer(_) >> empty()

		when:
			consumer.consumeRequest(request, response)

		then:
			0 * factory._
		and:
			1 * response.setStatusCode(SC_NOT_IMPLEMENTED)
			1 * response.setReasonPhrase(_)
	}

	def 'Always allow consumer to consume file request if consumer is returned from selector'() {
		given:
			def factory = Stub(HttpFileRequestFactory)
			def selector = Stub(HttpFileRequestConsumerSelector)
		and:
			def HttpRequestConsumer consumer = new HttpFileRequestConsumerDispatcher(factory, selector)
			def fileRequestConsumer = Mock(HttpFileRequestConsumer)
		and:
			def request = Stub(HttpRequest)
			def response = Mock(HttpResponse)
			def requestLine = Stub(RequestLine)
			def httpMethod = HttpMethod.GET
		and:
			def fileRequestMock = Mock(HttpFileRequest)
			selector.selectConsumer(httpMethod) >> present(fileRequestConsumer)
			selector.selectConsumer(_) >> empty()
			factory.createNew(request, response) >> fileRequestMock
		and:
			def httpMethodId = httpMethod.id
			request.getRequestLine() >> requestLine
			requestLine.getMethod() >> httpMethodId

		when:
			consumer.consumeRequest(request, response)

		then:
			1 * fileRequestConsumer.consumeFileRequest(fileRequestMock)
	}

	@Shared
	private mockedFactory = {
		Mock(HttpFileRequestFactory)
	}

	@Shared
	private mockedSelector = {
		Mock(HttpFileRequestConsumerSelector)
	}

}
