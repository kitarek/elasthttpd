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

package io.github.kitarek.elasthttpd.builder
import io.github.kitarek.elasthttpd.ElastHttpDBuilder
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer
import io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder
import spock.lang.Specification
import spock.lang.Unroll

class FluentElastHttpDBuilderSpec extends Specification {

	def 'FluentElastHttpDBuilder can be created without specyfing any parameters'() {
		when:
			new FluentElastHttpDBuilder()

		then:
			notThrown()
	}


	def 'Can specify server ID information that will be added to all responses'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			def builderInChain = builderUnderTest.serverInfo("MyServerInfo")

		then:
			builderInChain != null
			builderInChain == builderUnderTest
			notThrown()
	}

	@Unroll("Cannot specify the following invalid server ID information: '#info'")
	def 'Cannot specify invalid server ID information'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			builderUnderTest.serverInfo(info)

		then:
			thrown(exception)

		where:
			info | exception
			null | NullPointerException
			''   | IllegalArgumentException

	}

	def 'Can provide network settings assigning preconfigured NetworkConfigurationBuilder'() {
		given:
			def NetworkConfigurationBuilder networkConfigurationBuilder = Mock()
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			def builderInChain = builderUnderTest.networkConfiguration(networkConfigurationBuilder)

		then:
			builderInChain != null
			builderInChain == builderUnderTest
			notThrown()
	}

	def 'Cannot specify NULL NetworkConfigurationBuilder'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			builderUnderTest.networkConfiguration(null)

		then:
			thrown(NullPointerException)
	}

	def 'Cannot specify NULL custom request consumer'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			builderUnderTest.customRequestConsumer(null)

		then:
			thrown(NullPointerException)
	}

	def 'Can specify custom request consumer'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()
			def HttpRequestConsumer httpRequestConsumerMock = Mock()

		when:
			def builderInChain = builderUnderTest.customRequestConsumer(httpRequestConsumerMock)

		then:
			builderInChain != null
			builderInChain == builderUnderTest
			notThrown()
	}

	@Unroll('Cannot specify negative conurrent connections number: #invalidMaxNumberOfConcurrentConnections')
	def 'Cannot specify negative conurrent connections number'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			builderUnderTest.concurrentConnections(invalidMaxNumberOfConcurrentConnections)

		then:
			thrown(IllegalArgumentException)

		where:
			invalidMaxNumberOfConcurrentConnections << [-250, -1, 0]
	}

	def 'Can specify conurrent connections number greater than or equal to 1'() {
		given:
			def ElastHttpDBuilder builderUnderTest = new FluentElastHttpDBuilder()

		when:
			def builderInChain = builderUnderTest.concurrentConnections(maxNumberOfConcurrentConnections)

		then:
			builderInChain != null
			builderInChain == builderUnderTest
			notThrown()

		where:
			maxNumberOfConcurrentConnections << [1, 2, 100, 1024]
	}

}
