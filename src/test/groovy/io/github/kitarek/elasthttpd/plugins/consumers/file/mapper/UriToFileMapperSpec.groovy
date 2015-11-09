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

package io.github.kitarek.elasthttpd.plugins.consumers.file.mapper

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static java.util.UUID.randomUUID

class UriToFileMapperSpec extends Specification {

	@Unroll("#incorrectUriRequestPath is not correct URI request path")
	def 'Null, empty string not starting with / are not correct URI request paths'() {
		when:
			def isCorrect = UriToFileMapper.isCorrectUriRequestPath(incorrectUriRequestPath)

		then:
			isCorrect == false

		where:
			incorrectUriRequestPath << [null, 'aaa/', 'aaa', '', '  ', '  /aaa', ' /a/b/c']
	}

	@Unroll("#correctUriRequestPath is not correct URI request path")
	def 'Non-null, not empty string starting directly with / are correct URI request paths'() {
		when:
			def isCorrect = UriToFileMapper.isCorrectUriRequestPath(correctUriRequestPath)

		then:
			isCorrect == true

		where:
			correctUriRequestPath << ['/aaa/', '/aaa', '/', '/  ', '/  aaa', '/a /b / c']
	}


	def 'Never cannot create class instance given not existing directory'() {
		when:
			new UriToFileMapper("./not/existing/directory/" + randomUUID())

		then:
			thrown(IllegalArgumentException)
	}

	@Unroll("Never cannot create class instance given relative directory: [#relativeDir] even if exists")
	def 'Never cannot create class instance given relative directory even if exists'() {
		when:
			new UriToFileMapper(relativeDir)

		then:
			thrown(IllegalArgumentException)

		where:
			relativeDir << [".", "./"]
	}

	def 'Never cannot create class instance given absolute file even if exists'() {
		given:
			def existingFileInProject = "/build.gradle"

		when:
			new UriToFileMapper(validExistingDirectory() + existingFileInProject)

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot create class instance given empty directory path even if this means sth'() {
		when:
			new UriToFileMapper("")

		then:
			thrown(IllegalArgumentException)
	}

	def 'Always can create class instance given absolute and existing directory'() {
		given:
			def currentWorkingDirectory = validExistingDirectory()
			File currentWorkingDirectoryFileObject = new File(currentWorkingDirectory)

		expect: "current directory should always exists in normal circumstances"
			currentWorkingDirectoryFileObject.exists() && currentWorkingDirectoryFileObject.canRead()

		when:
			new UriToFileMapper(currentWorkingDirectory)

		then:
			notThrown()
	}

	def 'Never cannot map null URI request path into any directory'() {
		given:
			def rootMappedDirectory = validExistingDirectory()
			def UriToFileMapper mapper = new UriToFileMapper(rootMappedDirectory)

		when:
			mapper.mapUriRequestPath(null)

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot map empty URI request path into any directory'() {
		given:
			def rootMappedDirectory = validExistingDirectory()
			def UriToFileMapper mapper = new UriToFileMapper(rootMappedDirectory)

		when:
			mapper.mapUriRequestPath("")

		then:
			thrown(IllegalArgumentException)
	}

	def 'Never cannot map URI request path that does not start with /'() {
		given:
			def rootMappedDirectory = validExistingDirectory()
			def UriToFileMapper mapper = new UriToFileMapper(rootMappedDirectory)

		when:
			mapper.mapUriRequestPath("src")

		then:
			thrown(IllegalArgumentException)
	}

	def 'Maps / URI request path into rootDirectory'() {
		given:
			def rootMappedDirectory = validExistingDirectory()
			def UriToFileMapper mapper = new UriToFileMapper(rootMappedDirectory)

		when:
			String actualAbsolutePath = mapper.mapUriRequestPath('/')

		then:
			actualAbsolutePath == rootMappedDirectory
	}

	@Unroll("Maps specified URI request path: #requestedPath into 1:1 elements under rootDirectory")
	def 'Maps specified URI request path into 1:1 elements under rootDirectory'() {
		given:
			def UriToFileMapper mapper = new UriToFileMapper(root)

		when:
			def actualAbsoluteFilePath = mapper.mapUriRequestPath(requestedPath)

		then:
			actualAbsoluteFilePath == expectedMappedFilePath

		where:
			requestedPath | root                     | expectedMappedFilePath
			'/src'        | validExistingDirectory() | validExistingDirectory() + "/src"
			'/src/a/b/c'  | validExistingDirectory() | validExistingDirectory() + "/src/a/b/c"
	}

	@Unroll("Maps specified relative URI request path: #requestedPath appropriatly always under rootDirectory")
	def 'Maps specified relative URI request path appropriatly always under rootDirectory'() {
		given:
		def UriToFileMapper mapper = new UriToFileMapper(root)

		when:
		def actualAbsoluteFilePath = mapper.mapUriRequestPath(requestedPath)

		then:
		actualAbsoluteFilePath == expectedMappedFilePath

		where:
		requestedPath          | root                     | expectedMappedFilePath
		'/../src'              | validExistingDirectory() | validExistingDirectory() + "/src"
		'/../../../src/a/b/c'  | validExistingDirectory() | validExistingDirectory() + "/src/a/b/c"
		'/../../../src/a/b/c'  | validExistingDirectory() | validExistingDirectory() + "/src/a/b/c"
		'/./../../src/a/b/c'   | validExistingDirectory() | validExistingDirectory() + "/src/a/b/c"
		'/../src/../a/../b/c'  | validExistingDirectory() | validExistingDirectory() + "/b/c"
	}

	@Shared
	private validExistingDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}

}
