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

import java.nio.file.Path
import java.nio.file.Paths

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present
import static java.util.UUID.randomUUID
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED

class HttpDirectoryMappedFileRequestConsumerSpec extends Specification {

	@Unroll
	def 'Never cannot create class instance given null constructor arguments'() {
		when:
			new HttpDirectoryMappedFileRequestConsumer(rootDirectoryPath, requestFactory, consumerSelector)

		then:
			thrown(NullPointerException)

		where:
			rootDirectoryPath        | requestFactory  | consumerSelector
			validExistingDirectory() | null            | null
			null                     | mockedFactory() | null
			null                     | null            | mockedSelector()
			validExistingDirectory() | mockedFactory() | null
			validExistingDirectory() | null            | mockedSelector()
			null                     | mockedFactory() | mockedSelector()
			validExistingDirectory() | mockedFactory() | null
			null                     | null            | null
	}


	def 'Never cannot create class instance given not existing directory'() {
		when:
			new HttpDirectoryMappedFileRequestConsumer("./not/existing/directory/" + randomUUID(),
				mockedFactory(), mockedSelector())

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot create class instance given relative directory even if exists'() {
		when:
			new HttpDirectoryMappedFileRequestConsumer(".", mockedFactory(), mockedSelector())

		then:
			thrown(IllegalArgumentException)
	}

	def 'Always can create class instance given absolute and existeing directory'() {
		given:
			def currentWorkingDirectory = validExistingDirectory()
			File currentWorkingDirectoryFileObject = new File(currentWorkingDirectory)
		and:
			def mockedSelector = mockedSelector()
			def mockedFactory = mockedFactory()

		expect: "current directory should always exists in normal circumstances"
			currentWorkingDirectoryFileObject.exists() && currentWorkingDirectoryFileObject.canRead()

		when:
			new HttpDirectoryMappedFileRequestConsumer(currentWorkingDirectory, mockedFactory, mockedSelector)

		then:
			notThrown()
	}


	def 'Always pass well-known request method to consumer selector'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Mock(HttpFileRequestConsumerSelector)
			def directory = validExistingDirectory()
		and:
			def HttpRequestConsumer consumer = new HttpDirectoryMappedFileRequestConsumer(directory, factory, selector)
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
			def directory = validExistingDirectory()
		and:
			def HttpRequestConsumer consumer = new HttpDirectoryMappedFileRequestConsumer(directory, factory, selector)
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
			1 * factory.createNew(request, response, directory) >> Mock(HttpFileRequest)
			0 * factory._
	}

	def 'Always respond with method not implemented response when request method is unknown'() {
		given:
			def factory = Mock(HttpFileRequestFactory)
			def selector = Mock(HttpFileRequestConsumerSelector)
			def directory = validExistingDirectory()
		and:
			def HttpRequestConsumer consumer = new HttpDirectoryMappedFileRequestConsumer(directory, factory, selector)
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
			def directory = validExistingDirectory()
		and:
			def HttpRequestConsumer consumer = new HttpDirectoryMappedFileRequestConsumer(directory, factory, selector)
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
		and:
			def selector = Stub(HttpFileRequestConsumerSelector)
			def directory = validExistingDirectory()
		and:
			def HttpRequestConsumer consumer = new HttpDirectoryMappedFileRequestConsumer(directory, factory, selector)
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
			factory.createNew(request, response, directory) >> fileRequestMock
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
	private validExistingDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
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
