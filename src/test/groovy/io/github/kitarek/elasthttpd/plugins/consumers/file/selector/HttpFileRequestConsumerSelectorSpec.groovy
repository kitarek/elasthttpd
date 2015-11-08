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

package io.github.kitarek.elasthttpd.plugins.consumers.file.selector

import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumer
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumerFactory
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.model.HttpMethod.CONNECT
import static io.github.kitarek.elasthttpd.model.HttpMethod.DELETE
import static io.github.kitarek.elasthttpd.model.HttpMethod.GET
import static io.github.kitarek.elasthttpd.model.HttpMethod.HEAD
import static io.github.kitarek.elasthttpd.model.HttpMethod.OPTIONS
import static io.github.kitarek.elasthttpd.model.HttpMethod.PATCH
import static io.github.kitarek.elasthttpd.model.HttpMethod.POST
import static io.github.kitarek.elasthttpd.model.HttpMethod.PUT
import static io.github.kitarek.elasthttpd.model.HttpMethod.TRACE
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode.READ_AND_WRITE
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode.READ_ONLY

class HttpFileRequestConsumerSelectorSpec extends Specification {

	def 'Never can create selector instance without providing its mode or consumer factory'() {
		when:
			new HttpFileRequestConsumerSelector(mode, factory)

		then:
			thrown(NullPointerException)

		where:
			mode      | factory
			READ_ONLY | null
			null      | Mock(HttpFileRequestConsumerFactory)
			null      | null
	}


	def 'Always can create selector instance providing its READ_ONLY mode and consumer factory'() {
		given:
			def factory = Mock(HttpFileRequestConsumerFactory)

		when:
			new HttpFileRequestConsumerSelector(READ_ONLY, factory)

		then:
			notThrown()
		and:
			1 * factory.createConsumerForReadOperation()
			0 * factory._
	}

	def 'Always can create selector instance providing its READ_AND_WRITE mode and consumer factory'() {
		given:
			def factory = Mock(HttpFileRequestConsumerFactory)

		when:
			new HttpFileRequestConsumerSelector(READ_AND_WRITE, factory)

		then:
			notThrown()
		and:
			1 * factory.createConsumerForReadOperation()
			1 * factory.createConsumerForWriteOperation()
			1 * factory.createConsumerForDeleteOperation()
			0 * factory._
	}

	@Unroll("Selector created for read-only mode returns correct consumer -- #expectedIsConsumerPresent for #httpMethod")
	def 'Selector created for read-only mode returns correct consumer only for GET or HEAD requests'() {
		given:
			def factory = Stub(HttpFileRequestConsumerFactory)
		and:
			factory.createConsumerForReadOperation() >> expectedConsumer
		and:
			def selector = new HttpFileRequestConsumerSelector(READ_ONLY, factory)

		when:
			def optionalConsumer = selector.selectConsumer(httpMethod)

		then:
			optionalConsumer.isPresent() == expectedIsConsumerPresent
			(optionalConsumer.isPresent() && optionalConsumer.get() == expectedConsumer) == expectedIsConsumerPresent

		where:
			httpMethod | expectedIsConsumerPresent | expectedConsumer
			GET        | true                      | Mock(HttpFileRequestConsumer)
			HEAD       | true                      | Mock(HttpFileRequestConsumer)
			CONNECT    | false                     | null
			DELETE     | false                     | null
			OPTIONS    | false                     | null
			PATCH      | false                     | null
			POST       | false                     | null
			PUT        | false                     | null
			TRACE      | false                     | null
	}

	@Unroll("Selector created for read-and-write mode always returns correct read consumer for #httpMethod")
	def 'Selector created for read-and-write mode returns always correct read consumer only for GET, HEAD requests'() {
		given:
			def factory = Stub(HttpFileRequestConsumerFactory)
		and:
			factory.createConsumerForReadOperation() >> expectedReadOnlyConsumer
			factory.createConsumerForWriteOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForDeleteOperation() >> Mock(HttpFileRequestConsumer)
		and:
			def selector = new HttpFileRequestConsumerSelector(READ_AND_WRITE, factory)

		when:
		def optionalConsumer = selector.selectConsumer(httpMethod)

		then:
			optionalConsumer.isPresent() == true
			optionalConsumer.get() == expectedReadOnlyConsumer

		where:
			httpMethod | expectedReadOnlyConsumer
			GET        | Mock(HttpFileRequestConsumer)
			HEAD       | Mock(HttpFileRequestConsumer)
	}

	@Unroll("Selector created for read-and-write mode always returns correct write consumer for #httpMethod")
	def 'Selector created for read-and-write mode returns always correct write consumer only for PUT and POST requests'() {
		given:
			def factory = Stub(HttpFileRequestConsumerFactory)
		and:
			factory.createConsumerForReadOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForWriteOperation() >> expectedWriteConsumer
			factory.createConsumerForDeleteOperation() >> Mock(HttpFileRequestConsumer)
		and:
			def selector = new HttpFileRequestConsumerSelector(READ_AND_WRITE, factory)

		when:
			def optionalConsumer = selector.selectConsumer(httpMethod)

		then:
			optionalConsumer.isPresent() == true
			optionalConsumer.get() == expectedWriteConsumer

		where:
			httpMethod | expectedWriteConsumer
			PUT        | Mock(HttpFileRequestConsumer)
			POST       | Mock(HttpFileRequestConsumer)
	}


	def 'Selector created for read-and-write mode returns always correct delete consumer only for DELETE requests'() {
		given:
			def factory = Stub(HttpFileRequestConsumerFactory)
		and:
			def expectedDeleteConsumer = Mock(HttpFileRequestConsumer)
			factory.createConsumerForReadOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForWriteOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForDeleteOperation() >> expectedDeleteConsumer
		and:
			def selector = new HttpFileRequestConsumerSelector(READ_AND_WRITE, factory)

		when:
			def optionalConsumer = selector.selectConsumer(DELETE)

		then:
			optionalConsumer.isPresent() == true
			optionalConsumer.get() == expectedDeleteConsumer
	}

	@Unroll("Selector created for read-and-write mode never returns consumer for #httpMethod")
	def 'Selector created for read-and-write mode never returns consumer for TRACE, OPTIONS, ...'() {
		given:
			def factory = Stub(HttpFileRequestConsumerFactory)
		and:
			factory.createConsumerForReadOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForWriteOperation() >> Mock(HttpFileRequestConsumer)
			factory.createConsumerForDeleteOperation() >> Mock(HttpFileRequestConsumer)
		and:
			def selector = new HttpFileRequestConsumerSelector(READ_AND_WRITE, factory)

		when:
			def optionalConsumer = selector.selectConsumer(httpMethod)

		then:
			optionalConsumer.isNotPresent()

		where:
			httpMethod << [TRACE, OPTIONS, CONNECT, PATCH]
	}

}
