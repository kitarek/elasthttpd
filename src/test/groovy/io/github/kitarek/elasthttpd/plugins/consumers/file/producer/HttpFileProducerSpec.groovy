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

package io.github.kitarek.elasthttpd.plugins.consumers.file.producer

import org.apache.http.HttpResponse
import org.apache.http.entity.AbstractHttpEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

class HttpFileProducerSpec extends Specification {

	def 'Always can create instance without giving any parameters'() {
		when:
			new HttpFileProducer()
		then:
			notThrown()
	}

	@Unroll
	def 'Never cannot pass null local file or null HTTP response for sending via HTTP protocol'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();

		when:
			producer.sendFileOverHttpResponse(localFile, httpResponse)

		then:
			thrown(NullPointerException)

		where:
			localFile  | httpResponse
			null       | Mock(HttpResponse)
			Mock(File) | null
			null       | null
	}

	def 'Never cannot pass local file that does not exist'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();
			def File localFile = Stub(File)
			localFile.exists() >> false

		when:
			producer.sendFileOverHttpResponse(localFile, Mock(HttpResponse))

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot pass local file that is not a file (i.e. directory instead)'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();
			def File localFile = Stub(File)
			localFile.exists() >> true
			localFile.isFile() >> false

		when:
			producer.sendFileOverHttpResponse(localFile, Mock(HttpResponse))

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot pass local file that is not readable'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();
			def File localFile = Stub(File)
			localFile.exists() >> true
			localFile.isFile() >> true
			localFile.canRead() >> false

		when:
			producer.sendFileOverHttpResponse(localFile, Mock(HttpResponse))

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot pass local file that is a directory'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();
			def File localFile = Stub(File)
			localFile.exists() >> true
			localFile.isFile() >> false
			localFile.isDirectory() >> true
			localFile.canRead() >> true

		when:
			producer.sendFileOverHttpResponse(localFile, Mock(HttpResponse))

		then:
			thrown(IllegalArgumentException)
	}

	def 'Always can pass real local file that is a readable and existing one'() {
		given:
			def HttpFileProducer producer = new HttpFileProducer();
			def projectTestFilePath = "/src/test/resources/test-file.txt"
			def mimeType = 'text/plain'
			def File localRealTextFile = new File(currentExistingProjectDirectory() + projectTestFilePath)
			def filesize = localRealTextFile.size()
		and:
			def httpResponse = Mock(HttpResponse)
			def catchedEntity

		when:
			producer.sendFileOverHttpResponse(localRealTextFile, httpResponse)

		then:
			1 * httpResponse.setEntity(_) >> { args ->
				catchedEntity = args[0]
			}
		and:
			catchedEntity != null
			catchedEntity instanceof AbstractHttpEntity

		when:
			def AbstractHttpEntity catchedHttpEntity = catchedEntity

		then:
			catchedHttpEntity.contentType.value == mimeType
			catchedHttpEntity.contentLength == filesize
//			catchedHttpEntity.contentEncoding.value == null
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}
}
