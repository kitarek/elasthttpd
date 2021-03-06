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
import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory.HttpDirectoryRequestConsumer
import io.github.kitarek.elasthttpd.plugins.consumers.file.mapper.UriToFileMapper
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.RequestLine
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static java.util.UUID.randomUUID

class HttpFileReadRequestConsumerSpec extends Specification {

	def 'Can never create instance without giving extra dependencies or arguments'() {
		when:
			new HttpFileReadRequestConsumer()

		then:
			thrown(Exception)
	}

	@Unroll
	def 'Can never create instance when giving null HttpFileProducer/TemplatedHttpResponder/HttpDirectoryRequestConsumer'() {
		when:
			new HttpFileReadRequestConsumer(producer, responder, directoryRequestConsumer)

		then:
			thrown(NullPointerException)

		where:
			producer               | responder                     | directoryRequestConsumer
			null                   | Mock(TemplatedHttpResponder)  | null
			Mock(HttpFileProducer) | null                          | null
			null                   | null                          | null
			null                   | Mock(TemplatedHttpResponder)  | Mock(HttpDirectoryRequestConsumer)
			Mock(HttpFileProducer) | null                          | Mock(HttpDirectoryRequestConsumer)
			null                   | null                          | Mock(HttpDirectoryRequestConsumer)
			null                   | null                          | null
	}

	def 'Can always create instance when giving not-null valid HttpFileProducer object'() {
		when:
			new HttpFileReadRequestConsumer(Mock(HttpFileProducer), Mock(TemplatedHttpResponder),
					Mock(HttpDirectoryRequestConsumer))

		then:
			notThrown()
	}

	def 'Always respond with 404 code to read request for file that does not exist'() {
		given:
			def TemplatedHttpResponder templatedHttpResponder = Mock()
			def HttpDirectoryRequestConsumer directoryRequestConsumer = Mock()
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(Mock(HttpFileProducer),
					templatedHttpResponder, Mock(HttpDirectoryRequestConsumer))
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
			1 * templatedHttpResponder.respondWithResourceNotFound(response, _)
			0 * directoryRequestConsumer._
	}

	def 'Always use HttpDirectoryRequestConsumer to consume request for requested URI that are directories'() {
		given:
			def TemplatedHttpResponder templatedHttpResponder = Mock()
			def HttpDirectoryRequestConsumer directoryRequestConsumer = Mock()
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(Mock(HttpFileProducer),
					templatedHttpResponder, directoryRequestConsumer)

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
		and:
			def File capturedFile

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToDirectory
			1 * directoryRequestConsumer.serveExistingDirectoryElement(fileRequest, _) >> { args ->
				capturedFile = args[1]
			}
		and:
			capturedFile != null
			capturedFile.getAbsolutePath() == existingPathToDirectory
	}

	def 'Always use HttpFileProducer to generate response when there is a read request for file that exists'() {
		given:
			def HttpFileProducer producer = Mock()
			def TemplatedHttpResponder templatedHttpResponder = Mock()
			def HttpDirectoryRequestConsumer directoryRequestConsumer = Mock()
			def HttpFileRequestConsumer consumer = new HttpFileReadRequestConsumer(producer, templatedHttpResponder,
				directoryRequestConsumer)
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
