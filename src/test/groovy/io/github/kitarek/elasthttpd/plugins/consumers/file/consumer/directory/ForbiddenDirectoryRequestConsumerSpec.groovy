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

package io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory
import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.RequestLine
import spock.lang.Specification
import spock.lang.Unroll

class ForbiddenDirectoryRequestConsumerSpec extends Specification {

	def 'Cannot create directory consumer passing just null'() {
		when:
			new ForbiddenDirectoryRequestConsumer(null)
		then:
			thrown(NullPointerException)
	}

	def 'Can create directory consumer passing templatedHttpResponder'() {
		when:
			new ForbiddenDirectoryRequestConsumer(Mock(TemplatedHttpResponder))
		then:
			notThrown()
	}

	@Unroll
	def 'Never cannot serve when file request or file is null'() {
		given:
			def templatedHttpResponder = Mock(TemplatedHttpResponder)
			def consumer = new ForbiddenDirectoryRequestConsumer(templatedHttpResponder)

		when:
			consumer.serveExistingDirectoryElement(fileRequest, requestedDirectory)

		then:
			thrown(NullPointerException)

		where:
			fileRequest           | requestedDirectory
			Mock(HttpFileRequest) | null
			null                  | Mock(File)
			null                  | null
	}

	def 'Always serve responding with correct "FORBIDDEN" HTTP response template'() {
		given:
			def templatedHttpResponder = Mock(TemplatedHttpResponder)
			def consumer = new ForbiddenDirectoryRequestConsumer(templatedHttpResponder);
			def File directory = Mock()
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpRequest request = Mock()
			def HttpResponse response = Mock()
		and:
			fileRequest.request() >> request
			fileRequest.response() >> response
		and:
			def requestedUri = "/Existing/Dir"
			def RequestLine requestLine = Mock()
			requestLine.uri >> requestedUri
		and:
			request.getRequestLine() >> requestLine
		and:
			def capturedMessage

		when:
			consumer.serveExistingDirectoryElement(fileRequest, directory)

		then:
			1 * templatedHttpResponder.respondWithResourceForbidden(response, _) >> { args ->
				capturedMessage = args[1]
			}
		and:
			capturedMessage != null
			capturedMessage.contains(requestedUri)
	}

}
