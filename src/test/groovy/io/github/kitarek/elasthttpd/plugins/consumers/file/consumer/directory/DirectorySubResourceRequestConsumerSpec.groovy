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

class DirectorySubResourceRequestConsumerSpec extends Specification {

	def 'Can never create instance without giving extra dependencies or arguments'() {
		when:
			new DirectorySubResourceRequestConsumer()

		then:
			thrown(Exception)
	}

	@Unroll
	def 'Can never create instance when giving null HttpFileProducer/TemplatedHttpResponder/HttpDirectoryRequestConsumer'() {
		when:
			new DirectorySubResourceRequestConsumer(producer, responder, subResource)

		then:
			thrown(NullPointerException)

		where:
			producer               | responder                     | subResource
			null                   | Mock(TemplatedHttpResponder)  | null
			Mock(HttpFileProducer) | null                          | null
			null                   | null                          | null
			null                   | Mock(TemplatedHttpResponder)  | ""
			Mock(HttpFileProducer) | null                          | ""
			null                   | null                          | ""
			null                   | null                          | null
	}

	def 'Can always create instance when giving not-null valid HttpFileProducer object'() {
		when:
			new DirectorySubResourceRequestConsumer(Mock(HttpFileProducer), Mock(TemplatedHttpResponder),
					"")

		then:
			notThrown()
	}


	@Unroll
	def 'Never cannot serve when file request or file is null'() {
		given:
			def httpFileProducer = Mock(HttpFileProducer)
			def templatedHttpResponder = Mock(TemplatedHttpResponder)
			def consumer = new DirectorySubResourceRequestConsumer(httpFileProducer,
					templatedHttpResponder, "")

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

	@Unroll
	def 'Always respond with "NOT FOUND" responder template if default request do not exist or is directory'() {
		given:
			def TemplatedHttpResponder templatedHttpResponder = Mock()
			def DirectorySubResourceRequestConsumer consumer = new DirectorySubResourceRequestConsumer(Mock(HttpFileProducer),
					templatedHttpResponder, defaultSubResource)
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
			consumer.serveExistingDirectoryElement(fileRequest, new File(existingPathToDirectory))

		then:
			1 * templatedHttpResponder.respondWithResourceNotFound(response, _)

		where:
			defaultSubResource << [".", "src"]
	}

	def 'Always use HttpFileProducer to generate response when there is a read request for file that exists'() {
		given:
			def HttpFileProducer producer = Mock()
			def TemplatedHttpResponder templatedHttpResponder = Mock()
			def DirectorySubResourceRequestConsumer consumer = new DirectorySubResourceRequestConsumer(producer,
					templatedHttpResponder, "build.gradle")
			def HttpFileRequest fileRequest = Stub()
		and:
			def HttpRequest request = Mock()
			def HttpResponse response = Mock()
		and:
			fileRequest.request() >> request
			fileRequest.response() >> response
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
			consumer.serveExistingDirectoryElement(fileRequest, new File(currentExistingProjectDirectory()))

		then:
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
