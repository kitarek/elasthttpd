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
import io.github.kitarek.elasthttpd.commons.MimeTypeDetector
import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder
import org.apache.http.HttpResponse
import org.apache.http.entity.AbstractHttpEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present

class HttpFileProducerSpec extends Specification {

	@Unroll
	def 'Never cannot create an instance giving null parameters'() {
		when:
			new HttpFileProducer(detector, responder)
		then:
			thrown(NullPointerException)

		where:
			detector               | responder
			Mock(MimeTypeDetector) | null
			null                   | Mock(TemplatedHttpResponder)
			null                   | null
	}

	def 'Always can create an instance giving correct instance of MimeTypeDetector'() {
		given:
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()

		when:
			new HttpFileProducer(detector, responder)

		then:
			notThrown()
	}

	@Unroll
	def 'Never cannot pass null local file or null HTTP response for sending via HTTP protocol'() {
		given:
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);

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
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
			def File localFile = Stub(File)
			localFile.exists() >> false

		when:
			producer.sendFileOverHttpResponse(localFile, Mock(HttpResponse))

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot pass local file that is not a file (i.e. directory instead)'() {
		given:
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
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
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
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
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
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

	@Unroll
	def 'Always can pass real local file that is a readable and existing one'() {
		given:
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
			def projectTestFilePath = "/src/test/resources/test-file.txt"
			def File localRealTextFile = new File(currentExistingProjectDirectory() + projectTestFilePath)
			def filesize = localRealTextFile.size()
		and:
			def httpResponse = Mock(HttpResponse)
			def catchedEntity
		and:
			detector.detectMimeContentType(localRealTextFile) >> optionalMimeTypeDetected

		when:
			producer.sendFileOverHttpResponse(localRealTextFile, httpResponse)

		then:
			1 * httpResponse.setEntity(_) >> { args ->
				catchedEntity = args[0]
			}
			0 * responder._
		and:
			catchedEntity != null
			catchedEntity instanceof AbstractHttpEntity

		when:
			def AbstractHttpEntity catchedHttpEntity = catchedEntity

		then:
			catchedHttpEntity.contentType?.value == mimeType
			catchedHttpEntity.contentLength == filesize

		where:
			optionalMimeTypeDetected    | mimeType
			present(textFileMimeType()) | textFileMimeType()
			empty()                     | null
	}

	@Shared
	private def textFileMimeType = {
		'text/plain'
	}

	def 'Never can produce file when it is deleted during input stream creation'() {
		given:
			def MimeTypeDetector detector = Mock()
			def TemplatedHttpResponder responder = Mock()
			def HttpFileProducer producer = new HttpFileProducer(detector, responder);
		and:
			def httpResponse = Mock(HttpResponse)
		and:
			def File mockedFile = Mock()
			1 * mockedFile.exists() >> true
			1 * mockedFile.isDirectory() >> false
			1 * mockedFile.canRead() >> true
			1 * mockedFile.isFile() >> true
			mockedFile.getName() >> "Some file"
			mockedFile.getAbsolutePath() >> "Some absolute path"
		and: "We know that when creating file input stream getPath() is used to access a file so we can throw here"
			mockedFile.getPath() >> { throw new FileNotFoundException() }

		and:
			detector.detectMimeContentType(mockedFile) >> empty()

		when:
			producer.sendFileOverHttpResponse(mockedFile, httpResponse)

		then:
			1 * responder.respondWithInternalServerError(httpResponse, _)
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}
}
