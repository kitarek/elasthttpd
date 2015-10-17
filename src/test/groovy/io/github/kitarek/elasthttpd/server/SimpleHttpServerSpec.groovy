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

package io.github.kitarek.elasthttpd.server
import io.github.kitarek.elasthttpd.server.executors.ListenerExecutor
import io.github.kitarek.elasthttpd.server.listeners.HttpConnectionListener
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket
import spock.lang.Specification
import spock.lang.Unroll

class SimpleHttpServerSpec extends Specification {

	def 'Http Server can be only created by providing listening socket, HTTP connection listener and its executor'() {
		given:
			def executor = dummyExecutor()
			def listener = dummyListener()
			def socket = dummySocket()

		when:
			new SimpleHttpServer(executor, listener, socket);

		then:
			notThrown(Exception)
	}

	def 'Http Server cannot be created without providing anything'() {
		when:
			new SimpleHttpServer();

		then:
			thrown(Exception)
	}

	@Unroll
	def 'Http Server cannot be created without specifying listening socket or HTTP connection listener or listener executor'() {
		when:
			new SimpleHttpServer(executor, listener, socket)

		then:
			thrown(NullPointerException)

		where:
			executor        | listener        | socket
			null            | null            | null
			dummyExecutor() | null            | null
			null            | dummyListener() | null
			null            | null            | dummySocket()
			null            | dummyListener() | dummySocket()
			dummyExecutor() | null            | dummySocket()
			dummyExecutor() | dummyListener() | null
	}

	def 'Http Server can be started by initializing executor with listener and socket'() {
		given:
			def executorMock = Mock(ListenerExecutor)
			def listener = dummyListener()
			def socket = dummySocket()
		def HttpServer server = new SimpleHttpServer(executorMock, listener, socket)

		when:
			server.start()

		then:
			1 * executorMock.execute(listener, socket)
	}

	private dummyExecutor() {
		Mock(ListenerExecutor)
	}

	private dummyListener() {
		Mock(HttpConnectionListener)
	}

	private dummySocket() {
		Mock(ListeningSocket)
	}

}
