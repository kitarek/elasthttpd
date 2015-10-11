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

package io.github.kitarek.elasthttpd.model

import io.github.kitarek.elasthttpd.commons.Optional
import io.github.kitarek.elasthttpd.commons.OptionalMapper
import spock.lang.Specification

import static io.github.kitarek.elasthttpd.model.HttpMethod.*

class HttpMethodSpec extends Specification {

	def 'HttpMethod can be initialized from well known method strings'() {
		given:
			def HttpMethod actualMethod = null

		when:
			def Optional<HttpMethod> actualOptionalMethod = fromString(givenMethodString)
		and:
			actualOptionalMethod.map(new OptionalMapper<HttpMethod>() {
				@Override
				void present(HttpMethod object) {
					actualMethod = object;
				}
			})

		then:
			actualOptionalMethod.present == expectedMethodPresent
			actualMethod == expectedMethod

		where:
			givenMethodString | expectedMethodPresent | expectedMethod
			"GET"             | true                  | GET
			"POST"            | true                  | POST
			"PUT"             | true                  | PUT
			"HEAD"            | true                  | HEAD
			"DELETE"          | true                  | DELETE
			"OPTIONS"         | true                  | OPTIONS
			"TRACE"           | true                  | TRACE
			"CONNECT"         | true                  | CONNECT
			"head"            | false                 | null
			"tracert"         | false                 | null
	}
}
