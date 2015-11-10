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

package io.github.kitarek.elasthttpd.plugins.consumers.file.consumer
import io.github.kitarek.elasthttpd.plugins.consumers.file.mapper.UriToFileMapper
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.RequestLine
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

import static java.util.UUID.randomUUID
import static org.apache.http.HttpStatus.SC_NOT_FOUND

class HttpFileReadRequestConsumerSpec extends Specification {

	def 'Can never create instance without giving extra dependencies or arguments'() {
		when:
			new HttpFileReadRequestConsumer()

		then:
			thrown(Exception)
	}

	def 'Can never create instance when giving null HttpFileProducer'() {
		when:
			new HttpFileReadRequestConsumer(null)

		then:
			thrown(NullPointerException)
	}

	def 'Can always create instance when giving not-null valid HttpFileProducer object'() {
		when:
			new HttpFileReadRequestConsumer(Mock(HttpFileProducer))

		then:
			notThrown()
	}

	def 'Always respond with 404 code to read request for file that does not exist'() {
		given:
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(Mock(HttpFileProducer))
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpRequest request = Mock()
			def HttpResponse response = Mock()
			def UriToFileMapper mapper = Mock()
		and:
			fileRequest.request() >> request
			fileRequest.response() >> response
			fileRequest.mapper() >> mapper
		and:
			def requestedUri = "/Not/Existing/File"
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def notExistingPathToFile = currentExistingProjectDirectory() + "/not/existing/file/" + randomUUID()

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> notExistingPathToFile
			1 * response.setStatusCode(SC_NOT_FOUND)
			1 * response.setReasonPhrase("NOT FOUND")
	}

	def 'Always respond with 403 code to read request for requested URI that are directories'() {
		given:
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(Mock(HttpFileProducer))
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpRequest request = Mock()
			def HttpResponse response = Mock()
			def UriToFileMapper mapper = Mock()
		and:
			fileRequest.request() >> request
			fileRequest.response() >> response
			fileRequest.mapper() >> mapper
		and:
			def requestedUri = "/"
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def existingPathToDirectory = currentExistingProjectDirectory()

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToDirectory
			1 * response.setStatusCode(HttpStatus.SC_FORBIDDEN)
			1 * response.setReasonPhrase("FORBIDDEN")
	}

	def 'Always use HttpFileProducer to generate response when there is a read request for file that exists'() {
		given:
			def producer = Mock(HttpFileProducer)
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(producer)
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpRequest request = Mock()
			def HttpResponse response = Mock()
			def UriToFileMapper mapper = Mock()
		and:
			fileRequest.request() >> request
			fileRequest.response() >> response
			fileRequest.mapper() >> mapper
		and:
			def requestedUri = "/build.gradle"
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def existingPathToFile = currentExistingProjectDirectory() + "/build.gradle"
		and:
			def passedFileArgumentToProducer

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToFile
			1 * producer.sendFileOverHttpResponse(_, response) >> { args ->
				passedFileArgumentToProducer = args[0]
			}
		and:
			passedFileArgumentToProducer != null
			passedFileArgumentToProducer instanceof File
			passedFileArgumentToProducer.absolutePath == existingPathToFile
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}

}
