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

package io.github.kitarek.elasthttpd.commons

import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.entity.ByteArrayEntity
import spock.lang.Specification
import spock.lang.Unroll

class TemplatedHttpResponderSpec extends Specification {

	def 'Can create responder without giving any constructor arguments'() {
		when:
			new TemplatedHttpResponder()
		then:
			notThrown()
	}

	@Unroll
	def 'Cannot set entity if InternalServerError template is used with null response or message'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()

		when:
			responder.respondWithInternalServerError(response, message)

		then:
			thrown(NullPointerException)

		where:
			response           | message
			null               | ""
			Mock(HttpResponse) | null
			null               | null
	}

	def 'Set entity to specified message with correct status code and reason if InternalServerError template is used'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()
			def HttpResponse response = Mock()
			def catchedEntity
			def message = "M"

		when:
			responder.respondWithInternalServerError(response, message)

		then:
			1 * response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			1 * response.setReasonPhrase("INTERNAL SERVER ERROR")
			1 * response.setEntity(_) >> { args ->
				catchedEntity = args[0]
			}
		and:
			catchedEntity != null
			catchedEntity instanceof ByteArrayEntity

		when:
			def ByteArrayEntity catchedByteArrayEntity = catchedEntity
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
			catchedByteArrayEntity.writeTo(outputStream)

		then:
			outputStream.toString() == message
	}


	@Unroll
	def 'Cannot set entity if Not Found template is used with null response or message'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()

		when:
			responder.respondWithResourceNotFound(response, message)

		then:
			thrown(NullPointerException)

		where:
			response           | message
			null               | ""
			Mock(HttpResponse) | null
			null               | null
	}

	def 'Set entity to specified message with correct status code and reason if "Not Found" template is used'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()
			def HttpResponse response = Mock()
			def catchedEntity
			def message = "M"

		when:
			responder.respondWithResourceNotFound(response, message)

		then:
			1 * response.setStatusCode(HttpStatus.SC_NOT_FOUND)
			1 * response.setReasonPhrase("NOT FOUND")
			1 * response.setEntity(_) >> { args ->
				catchedEntity = args[0]
			}
		and:
			catchedEntity != null
			catchedEntity instanceof ByteArrayEntity

		when:
			def ByteArrayEntity catchedByteArrayEntity = catchedEntity
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
			catchedByteArrayEntity.writeTo(outputStream)

		then:
			outputStream.toString() == message
	}

	@Unroll
	def 'Cannot set entity if Resource Forbidden template is used with null response or message'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()

		when:
			responder.respondWithResourceForbidden(response, message)

		then:
			thrown(NullPointerException)

		where:
			response           | message
			null               | ""
			Mock(HttpResponse) | null
			null               | null
	}

	def 'Set entity to specified message with correct status code and reason if "Resource Forbidden" template is used'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()
			def HttpResponse response = Mock()
			def catchedEntity
			def message = "M"

		when:
			responder.respondWithResourceForbidden(response, message)

		then:
			1 * response.setStatusCode(HttpStatus.SC_FORBIDDEN)
			1 * response.setReasonPhrase("FORBIDDEN")
			1 * response.setEntity(_) >> { args ->
				catchedEntity = args[0]
			}
		and:
			catchedEntity != null
			catchedEntity instanceof ByteArrayEntity

		when:
			def ByteArrayEntity catchedByteArrayEntity = catchedEntity
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
			catchedByteArrayEntity.writeTo(outputStream)

		then:
			outputStream.toString() == message
	}


	def 'Cannot set code and reason if "DELETED" template is used with null response'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()

		when:
			responder.respondWithNoContentAndReasonDeleted(null)

		then:
			thrown(NullPointerException)
	}

	def 'Set correct status code and reason if "DELETE" template is used'() {
		given:
			def TemplatedHttpResponder responder = new TemplatedHttpResponder()
			def HttpResponse response = Mock()

		when:
			responder.respondWithNoContentAndReasonDeleted(response)

		then:
			1 * response.setStatusCode(HttpStatus.SC_NO_CONTENT)
			1 * response.setReasonPhrase("DELETED")
	}
}