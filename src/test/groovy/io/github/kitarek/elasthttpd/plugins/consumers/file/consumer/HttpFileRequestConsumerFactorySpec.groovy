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

import spock.lang.Specification

class HttpFileRequestConsumerFactorySpec extends Specification {

	def 'Always can create instance without parameters'() {
		when:
			new HttpFileRequestConsumerFactory()
		then:
			notThrown()
	}

	def 'Always can create read operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory()

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForReadOperation()

		then:
			consumer != null
			consumer instanceof HttpFileReadRequestConsumer
	}

	def 'Always can create write operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory()

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForWriteOperation()

		then:
			consumer != null
	}

	def 'Always can create delete operation file request consumer'() {
		given:
			def HttpFileRequestConsumerFactory factory = new HttpFileRequestConsumerFactory()

		when:
			def HttpFileRequestConsumer consumer = factory.createConsumerForDeleteOperation()

		then:
			consumer != null
	}

}
