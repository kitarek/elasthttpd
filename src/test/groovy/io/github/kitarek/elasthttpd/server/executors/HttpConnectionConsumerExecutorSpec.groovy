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

package io.github.kitarek.elasthttpd.server.executors

import io.github.kitarek.elasthttpd.server.consumers.HttpConnectionConsumer
import io.github.kitarek.elasthttpd.server.networking.NewConnection
import spock.lang.Specification

import java.util.concurrent.ExecutorService

class HttpConnectionConsumerExecutorSpec extends Specification {

	def 'Executor can be created with number of defined threads'() {
		given:
			def numberOfThreadsAvailable = 10;

		when:
			new HttpConnectionConsumerExecutor(numberOfThreadsAvailable);

		then:
			notThrown()
	}

	def 'Executor cannot be created with negative number of defined threads or 0'() {
		when:
			new HttpConnectionConsumerExecutor(numberOfThreadsAvailable);

		then:
			thrown(IllegalArgumentException)

		where:
			numberOfThreadsAvailable << [0, -1, -100]

	}

	def 'Executor invokes consumer using built-in thread executor'() {
		given:
			def ConsumerExecutor executorUnderTest = new HttpConnectionConsumerExecutor(1);
			def HttpConnectionConsumer consumer = Mock()
			def connection = Mock(NewConnection)
		and:
			def ExecutorService executorServiceStub = Stub()
			executorUnderTest.manyThreadsExecutor = executorServiceStub
		and:
			executorServiceStub.execute( { it instanceof Runnable} ) >> { runnableArgs -> runnableArgs[0].run() }

		when:
			executorUnderTest.execute(consumer, connection)

		then:
			1 * consumer.consumeConnection(connection)
	}

	def 'Executor does not invokes consumer when connection is null'() {
		given:
			def ConsumerExecutor executorUnderTest = new HttpConnectionConsumerExecutor(1);
			def HttpConnectionConsumer consumer = Mock()
			def connection = Mock(NewConnection)
		and:
			def ExecutorService executorServiceStub = Stub()
			executorUnderTest.manyThreadsExecutor = executorServiceStub
		and:
			executorServiceStub.execute( { it instanceof Runnable} ) >> { runnableArgs -> runnableArgs[0].run() }

		when:
			executorUnderTest.execute(consumer, null)

		then:
			0 * consumer.consumeConnection(_)
		and:
			thrown(NullPointerException)
	}

	def "Executor allows to stop consuming connections and it doesn't create new threads"() {
		given:
			def ConsumerExecutor executorUnderTest = new HttpConnectionConsumerExecutor(1);

		and:
			def ExecutorService executorServiceMock = Mock()
			executorUnderTest.manyThreadsExecutor = executorServiceMock

		when:
			executorUnderTest.terminate()

		then:
			1 * executorServiceMock.shutdown()
	}

}
