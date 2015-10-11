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

import spock.lang.Specification

import static io.github.kitarek.elasthttpd.commons.Optional.*

class OptionalSpec extends Specification {

	def 'Always can provide optional String value via optional() and get it back'() {
		given:
			def String value = "VALUE TEXT"

		when:
			def Optional<String> optionalString = optional(value);

		then:
			optionalString.present
			!optionalString.notPresent
			optionalString.get() == value
	}

	def 'Always can provide optional String value via present() and get it back'() {
		given:
			def String value = "VALUE TEXT"

		when:
			def Optional<String> optionalString = present(value);

		then:
			optionalString.present
			!optionalString.notPresent
			optionalString.get() == value
	}


	def 'Never cannot provide null value via present() method as it throws always exception immediately'() {
		given:
			def value = null

		when:
			present(value);

		then:
			thrown(NullPointerException)
	}

	def 'Always can provide null value/reference via empty() method and the value wont be present'() {

		when:
			def optionalValue = empty();

		then:
			optionalValue.notPresent
			!optionalValue.present
	}

	def 'Never can access null value/reference after initializing optional value using empty() method'(){
		when:
			def optionalValue = empty();
		and:
			optionalValue.get()

		then:
			thrown(NullPointerException)
	}

	def 'Never can access null value/reference after initializing optional using optional() method with null value'(){
		when:
			def optionalValue = optional(null);
		and:
			optionalValue.get()

		then:
			thrown(NullPointerException)
	}

	def 'Mapper will be always invoked for optional value if one is not null when calling map method with mapper'() {
		given:
			def String stringValue = "STRING"
			def OptionalMapper<String> mockedMapper = Mock(OptionalMapper)

		when:
			def Optional<String> optionalString = present(stringValue);
		and:
			optionalString.map(mockedMapper)

		then:
			1 * mockedMapper.present(stringValue)
	}

	def 'Mapper will be invoked twice for optional value if one is not null when calling map method with mapper twice'() {
		given:
			def String stringValue = "STRING"
			def OptionalMapper<String> mockedMapper = Mock(OptionalMapper)

		when:
			def Optional<String> optionalString = present(stringValue);
		and:
			optionalString.map(mockedMapper)
		and:
			optionalString.map(mockedMapper)

		then:
			2 * mockedMapper.present(stringValue)
	}

	def 'Mapper will NEVER be invoked for optional value if one is null when calling map method with mapper'() {
		given:
			def String stringValue = null
			def OptionalMapper<String> mockedMapper = Mock(OptionalMapper)

		when:
			def Optional<String> optionalString = optional(stringValue);
		and:
			optionalString.map(mockedMapper)
		and:
			optionalString.map(mockedMapper)

		then:
			0 * mockedMapper.present(_)
	}

	def 'Mapper will NEVER be invoked for optional value initialized with empty() when calling map method with mapper'() {
		given:
			def OptionalMapper<String> mockedMapper = Mock(OptionalMapper)

		when:
			def Optional<String> optionalString = empty();
		and:
			optionalString.map(mockedMapper)
		and:
			optionalString.map(mockedMapper)

		then:
			0 * mockedMapper.present(_)
	}

	def 'Dispatcher will be always invoked for present() and never for notPresent() for optional value if one is not null when calling dispatch method with dispatcher'() {
		given:
			def String stringValue = "STRING"
			def OptionalDispatcher<String> mockedDispatcher = Mock(OptionalDispatcher)

		when:
			def Optional<String> optionalString = present(stringValue);
		and:
			optionalString.dispatch(mockedDispatcher)

		then:
			1 * mockedDispatcher.present(stringValue)
			0 * mockedDispatcher.notPresent()
	}

	def 'Dispatcher will be invoked twice for present() and never for notPresent() for optional value if one is not null when calling dispatch method with dispatcher twice'() {
		given:
			def String stringValue = "STRING"
			def OptionalDispatcher<String> mockedDispatcher = Mock(OptionalDispatcher)

		when:
			def Optional<String> optionalString = present(stringValue);
		and:
			optionalString.dispatch(mockedDispatcher)
		and:
			optionalString.dispatch(mockedDispatcher)

		then:
			2 * mockedDispatcher.present(stringValue)
			0 * mockedDispatcher.notPresent()
	}

	def 'Dispatcher will NEVER be invoked for present() and 2 times for notPresent() for optional value if one is null when calling dispatch method with dispatcher'() {
		given:
			def String stringValue = null
			def OptionalDispatcher<String> mockedDispatcher = Mock(OptionalDispatcher)

		when:
			def Optional<String> optionalString = optional(stringValue);
		and:
			optionalString.dispatch(mockedDispatcher)
		and:
			optionalString.dispatch(mockedDispatcher)

		then:
			0 * mockedDispatcher.present(_)
			2 * mockedDispatcher.notPresent()
	}

	def 'Dispatcher will NEVER be invoked for present() and ALWAYS for notPresent() for optional value initialized with empty() when calling dispatch method with dispatcher'() {
		given:
			def OptionalDispatcher<String> mockedDispatcher = Mock(OptionalDispatcher)

		when:
			def Optional<String> optionalString = empty();
		and:
			optionalString.dispatch(mockedDispatcher)
		and:
			optionalString.dispatch(mockedDispatcher)

		then:
			0 * mockedDispatcher.present(_)
			2 * mockedDispatcher.notPresent()
	}


}
