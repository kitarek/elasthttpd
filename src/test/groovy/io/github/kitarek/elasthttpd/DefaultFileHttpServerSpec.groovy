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

package io.github.kitarek.elasthttpd

import io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder
import org.easymock.EasyMock
import org.junit.Rule
import org.powermock.api.easymock.PowerMock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import spock.lang.Specification

import static org.powermock.api.easymock.PowerMock.mockStatic

@PrepareForTest([ElastHttpD.class, FileServerPluginBuilder.class])
class DefaultFileHttpServerSpec extends Specification {

	@Rule PowerMockRule powerMockRule = new PowerMockRule();

	def 'The default file HTTP server can be run passing fileserver plugin builder'() {
		given:
			mockStatic(ElastHttpD.class)
			mockStatic(FileServerPluginBuilder.class)
		and:
			def args = [] as String[];
			def ElastHttpDBuilder builder = Mock()
			def FileServerPluginBuilder fileServerPluginBuilderMock = Mock()
		and:
			EasyMock.expect(ElastHttpD.startBuilding()).andReturn(builder).once();
			EasyMock.expect(FileServerPluginBuilder.fileServer()).andReturn(fileServerPluginBuilderMock).once();
			EasyMock.expect(FileServerPluginBuilder.currentDirectory()).andReturn(".").atLeastOnce();
			PowerMock.replayAll()

		when:
			DefaultFileHttpServer.main(args)

		then:
			PowerMock.verifyAll()
		and:
			1 * builder.consumeRequestsWithPlugin(fileServerPluginBuilderMock) >> builder
			1 * builder.run()
			0 * builder._
		and:
			1 * fileServerPluginBuilderMock.withRootServerDirectory(".") >> fileServerPluginBuilderMock
	}
}

