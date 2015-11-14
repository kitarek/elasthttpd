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

class HttpFileDeleteRequestConsumerSpec extends Specification {


	public static final String DIR_WITH_TEST_RESOURCES = "/src/test/resources/"

	def 'Always can create delete consumer with TemplatedHttpResponder'() {
		when:
			new HttpFileDeleteRequestConsumer(Mock(TemplatedHttpResponder))
		then:
			notThrown()
	}

	def 'Never cannot create delete consumer with null dependency'() {
		when:
			new HttpFileDeleteRequestConsumer(null)
		then:
			thrown(NullPointerException)
	}

	def 'Throws NullPointerException when null HTTP file request is passed to consumer'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileDeleteRequestConsumer(responderMock)

		when:
			consumer.consumeFileRequest(null)

		then:
			thrown(NullPointerException)
	}

	def 'Always respond with NOT FOUND template to delete request for file that does not exist'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileDeleteRequestConsumer(responderMock)
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
			1 * responderMock.respondWithResourceNotFound(response, _)
	}

	def 'Always respond with FORBIDDEN template to delete request for requested URI that are directories'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileDeleteRequestConsumer(responderMock)
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
			def requestedUri = "/src/test/resources"
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
			1 * responderMock.respondWithResourceForbidden(response, _)
	}

	def 'Always delete the file and generate "NO CONTENT" response with reason "DELETED" when there is a delete request for the file that exists'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileDeleteRequestConsumer(responderMock)
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
			def existingPathToFile = currentExistingProjectDirectory() + DIR_WITH_TEST_RESOURCES + randomUUID()
			new File(existingPathToFile).createNewFile()

		expect:
			new File(existingPathToFile).exists()

		when:
			consumer.consumeFileRequest(fileRequest)

		then:
			1 * mapper.mapUriRequestPath(requestedUri) >> existingPathToFile
		and:
			new File(existingPathToFile).exists() == false
		and:
			1 * responderMock.respondWithNoContentAndReasonDeleted(response)


		cleanup:
			new File(existingPathToFile).delete()
	}

	def 'When file deletion is unsuccsesful respond with Internal Server Error'() {
		given:
			def responderMock = Mock(TemplatedHttpResponder)
			def HttpFileRequestConsumer consumer = new HttpFileDeleteRequestConsumer(responderMock)
		and:
			def HttpResponse mockedResponse = Mock()
			def HttpRequest mockedRequest = Mock()
		and:
			def HttpFileRequest fileRequest = Stub()
			fileRequest.response() >> mockedResponse
			fileRequest.request() >> mockedRequest
		and:
			def requestedUri = "/some/requested/uri"
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			mockedRequest.getRequestLine() >> requestLine
		and:
			def mockedFile = Mock(File)
			def capturedInternalServerErrorMessage

		when:
			consumer.tryDeleteFileAndRespondToRequest(fileRequest, mockedFile)

		then:
			1 * mockedFile.delete() >> false
			0 * mockedFile._
		and:
			1 * responderMock.respondWithInternalServerError(mockedResponse, _) >> { args ->
				capturedInternalServerErrorMessage = args[1]
			}
			0 * responderMock._
		and:
			capturedInternalServerErrorMessage != null
			capturedInternalServerErrorMessage.contains(requestedUri)
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}

}
