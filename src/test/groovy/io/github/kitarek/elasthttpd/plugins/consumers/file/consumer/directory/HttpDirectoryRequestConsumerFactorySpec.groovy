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
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer
import spock.lang.Specification

class HttpDirectoryRequestConsumerFactorySpec extends Specification {

	def 'Cannot initalize HttpDirectoryRequestConsumerFactory without dependency'() {
		when:
			new HttpDirectoryRequestConsumerFactory()
		then:
			thrown(Exception)
	}

	def 'Cannot initalize HttpDirectoryRequestConsumerFactory with null dependencies'() {
		when:
			new HttpDirectoryRequestConsumerFactory(responder, producer)

		then:
			thrown(NullPointerException)

		where:
			responder                    | producer
			null                         | Mock(HttpFileProducer)
			Mock(TemplatedHttpResponder) | null
			null                         | null
	}

	def 'Always can create subresource access directory request consumer if subresource is not null'() {
		given:
			def HttpDirectoryRequestConsumerFactory factory = new HttpDirectoryRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpFileProducer))

		when:
			def HttpDirectoryRequestConsumer consumer = factory.createConsumerThatAllowsToAccessSubResourceForDirectories("")

		then:
			consumer != null
			consumer instanceof DirectorySubResourceRequestConsumer
	}

	def 'Never can create subresource access directory request consumer if subresource is null'() {
		given:
			def HttpDirectoryRequestConsumerFactory factory = new HttpDirectoryRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpFileProducer))

		when:
			factory.createConsumerThatAllowsToAccessSubResourceForDirectories(null)

		then:
			thrown(NullPointerException)
	}


	def 'Always can create forbidden access directory request consumer'() {
		given:
			def HttpDirectoryRequestConsumerFactory factory = new HttpDirectoryRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpFileProducer)
			)

		when:
			def HttpDirectoryRequestConsumer consumer = factory.createConsumerThatForbidsAccessToDirectories();

		then:
			consumer != null
			consumer instanceof ForbiddenDirectoryRequestConsumer
	}

}
