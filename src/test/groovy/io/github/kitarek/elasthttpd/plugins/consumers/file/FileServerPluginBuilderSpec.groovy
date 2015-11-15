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

package io.github.kitarek.elasthttpd.plugins.consumers.file

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder.currentDirectory

class FileServerPluginBuilderSpec extends Specification {

	def 'Can create instance using instance static method'() {
		when:
			def instance = FileServerPluginBuilder.fileServer()

		then:
			instance != null
			instance instanceof FileServerPluginBuilder
	}

	def 'Never cannot build default consumer using only build method'() {
		when:
			FileServerPluginBuilder.fileServer().build()
		then:
			thrown(NullPointerException)
	}

	def 'Always can build default consumer using currentDirectory for withRootServerDirectory'() {
		when:
			def consumer = FileServerPluginBuilder.fileServer().withRootServerDirectory(currentDirectory()).build()

		then:
			consumer != null
			consumer instanceof HttpFileRequestConsumerDispatcher
	}

	def 'Never cannot use allowFileOperations with null argument'() {
		when:
			FileServerPluginBuilder.fileServer().allowFileOperations(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll
	def 'Always can use allowFileOperations with not null correct FileMode argument specifying current directory as root server directory'() {
		when:
			def consumer = FileServerPluginBuilder.fileServer().
					withRootServerDirectory(currentDirectory()).allowFileOperations(fileMode).build()

		then:
			consumer != null
			consumer instanceof HttpFileRequestConsumerDispatcher

		where:
			fileMode << [FileServerMode.READ_ONLY, FileServerMode.READ_AND_WRITE]
	}

	def 'Never cannot use null arguemnt for withRootServerDirectory'() {
		when:
			FileServerPluginBuilder.fileServer().withRootServerDirectory(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll
	def 'Never cannot use non existing directory for withRootServerDirectory'() {
		when:
			FileServerPluginBuilder.fileServer().withRootServerDirectory(directoryPath)

		then:
			thrown(IllegalArgumentException)

		where:
			directoryPath << [ validExistingDirectory() + "/README.md", "----" ]
	}

	@Unroll("Always can use existing, readable directory as withRootServerDirectory: #directoryPath")
	def 'Always can use existing, readable directory as withRootServerDirectory'() {
		when:
			def consumer = FileServerPluginBuilder.fileServer().withRootServerDirectory(directoryPath).build()

		then:
			consumer != null
			consumer instanceof HttpFileRequestConsumerDispatcher

		where:
			directoryPath << [ ".", "/", validExistingDirectory() + "/..", "./src",
							   validAbsoluteExistingDirectory(), validAbsoluteExistingDirectory() + "/..",
							   validAbsoluteExistingDirectory() + "/src"]
	}

	def 'Returns currently working directory that can be used for withRootServerDirectory'() {
		when:
			def actualAbsoluteCurrentDirectory = currentDirectory()

		then:
			actualAbsoluteCurrentDirectory == validAbsoluteExistingDirectory()
	}

	def 'Always can provide build default consumer using currentDirectory for withRootServerDirectory and forbidsAccessToDirectories'() {
		when:
			def consumer = FileServerPluginBuilder.fileServer()
					.withRootServerDirectory(currentDirectory())
					.forbidsAccessToDirectories()
					.build()

		then:
			consumer != null
			consumer instanceof HttpFileRequestConsumerDispatcher
	}

	def 'Always can provide build default consumer using currentDirectory for withRootServerDirectory and serveSubresourceWhenDirectoryRequested'() {
		when:
		def consumer = FileServerPluginBuilder.fileServer()
				.withRootServerDirectory(currentDirectory())
				.serveSubresourceWhenDirectoryRequested("")
				.build()

		then:
			consumer != null
			consumer instanceof HttpFileRequestConsumerDispatcher
	}

	def 'Never cannot null subresource for serveSubresourceWhenDirectoryRequested'() {
		when:
			FileServerPluginBuilder.fileServer().serveSubresourceWhenDirectoryRequested(null)

		then:
			thrown(NullPointerException)
	}

	@Shared
	private validExistingDirectory = {
		Path currentRelativePath = Paths.get(".");
		currentRelativePath.toString();
	}

	@Shared
	private validAbsoluteExistingDirectory = {
		Path currentRelativePath = Paths.get("");
		currentRelativePath.toAbsolutePath().toString();
	}

}

