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
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory.HttpDirectoryRequestConsumer
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer
import spock.lang.Specification

class HttpFileRequestConsumerFactorySpec extends Specification {

	def 'Never can create instance without parameters'() {
		when:
			new HttpFileRequestConsumerFactory()
		then:
			thrown(Exception)
	}

	def 'Never can create instance with null dependencies'() {
		when:
			new HttpFileRequestConsumerFactory(responder, consumer, producer)

		then:
			thrown(NullPointerException)

		where:
			responder                    | consumer                           | producer
			null                         | Mock(HttpDirectoryRequestConsumer) | Mock(HttpFileProducer)
			Mock(TemplatedHttpResponder) | null                               | Mock(HttpFileProducer)
			Mock(TemplatedHttpResponder) | Mock(HttpDirectoryRequestConsumer) | null
			Mock(TemplatedHttpResponder) | null                               | null
			null                         | Mock(HttpDirectoryRequestConsumer) | null
			null                         | null                               | Mock(HttpFileProducer)
			null                         | null                               | null
	}

	def 'Always can create instance with not-null dependencies'() {
		when:
			new HttpFileRequestConsumerFactory(
				Mock(TemplatedHttpResponder), Mock(HttpDirectoryRequestConsumer), Mock(HttpFileProducer))
		then:
			notThrown()
	}

	def 'Always can create read operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpDirectoryRequestConsumer), Mock(HttpFileProducer))

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForReadOperation()

		then:
			consumer != null
			consumer instanceof HttpFileReadRequestConsumer
	}

	def 'Always can create write operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpDirectoryRequestConsumer), Mock(HttpFileProducer))

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForWriteOperation()

		then:
			consumer != null
			consumer instanceof HttpFileWriteRequestConsumer
	}

	def 'Always can create delete operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory(
					Mock(TemplatedHttpResponder), Mock(HttpDirectoryRequestConsumer), Mock(HttpFileProducer))

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForDeleteOperation()

		then:
			consumer != null
			consumer instanceof HttpFileDeleteRequestConsumer
	}

}
