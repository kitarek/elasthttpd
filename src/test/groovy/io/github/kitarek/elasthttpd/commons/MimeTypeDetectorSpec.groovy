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

import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

import static java.util.UUID.randomUUID

class MimeTypeDetectorSpec extends Specification {

	def 'Can create mime type detector without giving any arguments'() {
		when:
			new MimeTypeDetector()

		then:
			notThrown()
	}

	def 'Can detect mime/type of directory'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()
			def file = new File(currentExistingProjectDirectory())

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(file)

		then:
			optionalMimeType.isPresent()
			optionalMimeType.get() == "inode/directory"
	}

	def 'Cannot detect mime/type of not existing file/directory'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()
			def file = new File(currentExistingProjectDirectory() + "/not/existing/" + randomUUID())

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(file)

		then:
			optionalMimeType.isNotPresent()
	}

	def 'Cannot detect mime/type of null file'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(null)

		then:
			thrown(NullPointerException)
	}

	def 'Cannot detect mime/type of incorrect file'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(new File("/w@!#@!#dwefwgowjpowjgpwfe/wfwefewf"))

		then:
			optionalMimeType.isNotPresent()
	}

	def 'Can detect mime/type of existing text file as text/plain'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()
			def file = new File(currentExistingProjectDirectory() + "/src/test/resources/test-file.txt")

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(file)

		then:
			optionalMimeType.isPresent()
			optionalMimeType.get() == "text/plain"
	}

	def 'Cannot detect mime/type when IOException is thrown by probe method'() {
		given:
			MimeTypeDetector detector = new MimeTypeDetector()
			def File file = Stub()
			file.getAbsolutePath() >> { throw new IOException() }

		when:
			Optional<String> optionalMimeType = detector.detectMimeContentType(file)

		then:
			optionalMimeType.isNotPresent()
	}

	@Shared
	private currentExistingProjectDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}
}
