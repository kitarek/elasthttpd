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

package io.github.kitarek.elasthttpd.plugins.consumers.file.request

import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

class HttpFileRequestFactorySpec extends Specification {

	def 'Can never create factory with null arguments'() {
		when:
			new HttpFileRequestFactory(null)

		then:
			thrown(NullPointerException)
	}

	def 'Can always create factory with valid existing directory'() {
		when:
			new HttpFileRequestFactory(validExistingDirectory())

		then:
			notThrown()
	}

	@Unroll
	def 'Cannot create file request without specifying request or response'() {
		given:
			def HttpFileRequestFactory factory = new HttpFileRequestFactory(validExistingDirectory())

		when:
			factory.createNew(request, response)

		then:
			thrown(NullPointerException)

		where:
			request       | response
			mockRequest() | null
			null          | mockResponse()
			null          | null
	}

	def 'Can create file request specifying both request and response'() {
		given:
			def HttpFileRequestFactory factory = new HttpFileRequestFactory(validExistingDirectory())
			def request = mockRequest()
			def response = mockResponse()

		when:
			def HttpFileRequest actualCreatedHttpFileRequest = factory.createNew(request, response)

		then:
			notThrown()
		and:
			actualCreatedHttpFileRequest != null
			actualCreatedHttpFileRequest.mapper() != null
			actualCreatedHttpFileRequest.request() != null
			actualCreatedHttpFileRequest.response() != null
	}

	@Shared
	private validExistingDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}

	@Shared
	private mockRequest = {
		Mock(HttpRequest)
	}

	@Shared
	private mockResponse = {
		Mock(HttpResponse)
	}
}
