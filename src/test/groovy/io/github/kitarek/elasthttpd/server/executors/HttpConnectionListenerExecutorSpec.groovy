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
import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket
import spock.lang.Specification

import java.util.concurrent.ExecutorService

class HttpConnectionListenerExecutorSpec extends Specification {

	def ListenerExecutor executorUnderTest;

	def setup() {
		executorUnderTest = new HttpConnectionListenerExecutor();
	}

	def 'Executor invokes listener using built-in thread executor'() {
		given:
			def listener = Mock(HttpConnectionListener)
			def socket = Mock(ListeningSocket)
		and:
			def ExecutorService executorServiceStub = Stub()
			executorUnderTest.oneThreadExecutor = executorServiceStub
		and:
			executorServiceStub.execute( { it instanceof Runnable} ) >> { runnableArgs -> runnableArgs[0].run() }

		when:
			executorUnderTest.execute(listener, socket)

		then:
			1 * listener.listenAndPassNewConnections(socket)
	}
}
