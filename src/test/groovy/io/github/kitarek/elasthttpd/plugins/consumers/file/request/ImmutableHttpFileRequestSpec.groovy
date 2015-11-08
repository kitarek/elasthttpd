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

import io.github.kitarek.elasthttpd.plugins.consumers.file.mapper.UriToFileMapper
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ImmutableHttpFileRequestSpec extends Specification {

	@Unroll("Never cannot create immutable file request instance if constructor arguments are as follows: [#request, #response, #mapper]")
	def 'Never cannot create immutable file request instance if any of constructor argument is null'() {
		when:
			new ImmutableHttpFileRequest(request, response, mapper)

		then:
			thrown(NullPointerException)

		where:
			request           | response           | mapper
			mockRequest()     | mockResponse()     | null
			mockRequest()     | null               | mockMapper()
			null              | mockResponse()     | mockMapper()
			mockRequest()     | null               | null
			null              | mockResponse()     | null
			null              | null               | mockMapper()
			null              | null               | null
	}

	def 'Always can create immutable file request instance if none of constructor argument is null'() {
		given:
			def request = mockRequest()
			def response = mockResponse()
			def mapper = mockMapper()

		when:
			def instance = new ImmutableHttpFileRequest(request, response, mapper)

		then:
			instance.request() == request
			instance.mapper() == mapper
			instance.response() == response
	}

	@Shared
	private mockResponse = {
		Mock(HttpResponse)
	}

	@Shared
	private mockRequest = {
		Mock(HttpRequest)
	}

	@Shared
	private mockMapper = {
		Mock(UriToFileMapper)
	}

}
