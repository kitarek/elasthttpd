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
import org.easymock.EasyMock
import org.junit.Rule
import org.powermock.api.easymock.PowerMock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import spock.lang.Specification

import static org.powermock.api.easymock.PowerMock.mockStatic

@PrepareForTest([ElastHttpD.class])
class DefaultHttpServerSpec extends Specification {

	@Rule PowerMockRule powerMockRule = new PowerMockRule();

	def 'The default HTTP server can be run without need to do anything else and it builds default HTTP server'() {
		given:
			mockStatic(ElastHttpD.class)
		and:
			def args = [] as String[];
			def builder = Mock(ElastHttpDBuilder)
		and:
			EasyMock.expect(ElastHttpD.startBuilding()).andReturn(builder).once();
			PowerMock.replayAll()

		when:
			DefaultHttpServer.main(args)

		then:
			PowerMock.verifyAll()
		and:
			1 * builder.run()
	}
}
