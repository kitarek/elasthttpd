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
import io.github.kitarek.elasthttpd.plugins.consumers.file.mapper.UriToFileMapper
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.RequestLine
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

import static java.util.UUID.randomUUID

class HttpFileWriteRequestConsumerSpec extends Specification {

	public static final String DIR_WITH_TEST_RESOURCES = "/src/test/resources/"
	public static final String RESOURCE_PATH_SEPARATOR_STRING = "/"

	def 'Always can create delete consumer with TemplatedHttpResponder'() {
		when:
			new HttpFileWriteRequestConsumer(Mock(TemplatedHttpResponder))
		then:
			notThrown()
	}

	def 'Never cannot create delete consumer with null dependency'() {
		when:
			new HttpFileWriteRequestConsumer(null)
		then:
			thrown(NullPointerException)
	}

	def 'Throws NullPointerException when null HTTP file request is passed to consumer'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)

		when:
			consumer.consumeFileRequest(null)

		then:
			thrown(NullPointerException)
	}

	def 'Always respond with FORBIDDEN template to write request for requested URI that are directories'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)
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
			def requestedUri = DIR_WITH_TEST_RESOURCES
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def existingPathToDirectory = currentExistingProjectDirectory() + DIR_WITH_TEST_RESOURCES

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToDirectory
			1 * responderMock.respondWithResourceForbidden(response, _)
	}

	def 'Always creates an empty file and respond with "CREATED" status template to write request *without entity* for requested URI that is existing path but not to an existing directory'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)
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
			def requestedUri = DIR_WITH_TEST_RESOURCES + randomUUID()
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def existingPathToFile = currentExistingProjectDirectory() + DIR_WITH_TEST_RESOURCES + randomUUID()

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToFile
			1 * responderMock.respondThatResourceIsCreated(response)
			// TODO the rest to be checked with entity later
		and:
			new File(existingPathToFile).exists()

		cleanup:
			new File(existingPathToFile).delete()
	}

	def 'Always respond with NOT FOUND status template to write request *without entity* for requested URI that is not an existing path to file'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)
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
			def requestedUri = DIR_WITH_TEST_RESOURCES + randomUUID() + RESOURCE_PATH_SEPARATOR_STRING + randomUUID()
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def notExistingPathToNotExistingFile = currentExistingProjectDirectory() + requestedUri
		and:
			def capturedMessage

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> notExistingPathToNotExistingFile
			1 * responderMock.respondWithResourceNotFound(response, _) >> { args ->
				capturedMessage = args[1]
			}
		and:
			capturedMessage != null
			capturedMessage.contains(requestedUri)
	}

	def 'When creating an empty file and that file still does not exist respond with "Internal Server Error" template'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpResponse response = Mock()
			def HttpRequest request = Mock()
		and:
			fileRequest.response() >> response
			fileRequest.request() >> request
		and:
			def File mockedFile = Mock()
			1 * mockedFile.exists() >> false
		and:
			def requestedUri = RESOURCE_PATH_SEPARATOR_STRING  + randomUUID()
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def capturedMessage

		when:
			consumer.postprocessCreationOfFile(fileRequest, mockedFile)

		then:
			1 * responderMock.respondWithInternalServerError(response, _) >> { args ->
				capturedMessage = args[1]
			}
		and:
			capturedMessage != null
			capturedMessage.contains(requestedUri)
	}

	def 'Closing the stream itself is not fatal for request processing'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileWriteRequestConsumer(responderMock)
			def HttpFileRequest fileRequest = Stub()
			def File file = Mock()
			def OutputStream outputStream = Mock()

		when:
			consumer.closeTheStream(outputStream, file)

		then:
			1 * outputStream.close() >> {
				throw new IOException();
			}
		and:
			notThrown()
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}
}
