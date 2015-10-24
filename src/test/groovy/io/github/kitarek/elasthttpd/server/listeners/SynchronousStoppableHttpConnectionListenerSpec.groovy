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

package io.github.kitarek.elasthttpd.server.listeners

import io.github.kitarek.elasthttpd.model.ServerState
import io.github.kitarek.elasthttpd.server.consumers.HttpConnectionConsumer
import io.github.kitarek.elasthttpd.server.executors.ConsumerExecutor
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket
import io.github.kitarek.elasthttpd.server.networking.NewConnection
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present
import static io.github.kitarek.elasthttpd.model.ServerState.RUNNING
import static io.github.kitarek.elasthttpd.model.ServerState.STOPPED
import static io.github.kitarek.elasthttpd.model.ServerState.STOPPING

class SynchronousStoppableHttpConnectionListenerSpec extends Specification {

	def conditions = new PollingConditions(timeout: 10)

	def 'Listener can be created by providing consumer and its executor'() {
		given:
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
		when:
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
		then:
			notThrown(Exception)
		and:
			listener.state == STOPPED
	}

	def 'Listener cannot be created without providing anything'() {
		when:
			new SynchronousStoppableHttpConnectionListener()
		then:
			thrown(Exception)
	}

	def 'Listener cannot be created without providing consumer executor or connection consumer'() {
		given:
			def ConsumerExecutor executor = providedExecutor
			def HttpConnectionConsumer consumer = providedConsumer

		when:
			new SynchronousStoppableHttpConnectionListener(executor, consumer)

		then:
			thrown(Exception)

		where:
			providedExecutor        | providedConsumer
			null	                    | Mock(HttpConnectionConsumer)
			Mock(ConsumerExecutor)  | null
			null                    | null
	}

	def 'Listener cannot be started when socket is null'() {
		given:
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = null

		when:
			listener.listenAndPassNewConnections(socket)

		then:
			thrown(NullPointerException)
		and:
			listener.state == STOPPED
	}

	def 'Listener can start listening on socket and handles new connection which is passed to consumer executor and then it is interrupted'() {
		given:
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = Mock()
			def NewConnection newConnection = Mock()

		when:
			listener.listenAndPassNewConnections(socket)

		then:
			1 * socket.listenForANewConnection() >> present(newConnection)
		and: "Executor need to throw exception to stop constant listening for test purposes"
			1 * executor.execute(consumer, newConnection) >> { throw new RuntimeException() }
		and:
			thrown(RuntimeException)
		and:
			listener.state == STOPPED
	}


	def 'Listener can start listening on socket and handles all new connections which are passed to consumer executor until timeout passes'() {
		given:
			def oneHundredOfMiliseconds = 100
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = Mock()
			def NewConnection newConnection = Mock()

		expect:
			listener.state == STOPPED

		when:
			listener.listenAndPassNewConnections(socket)

		then: "Should listen 10 times for new connections"
			10 * socket.listenForANewConnection() >> present(newConnection)
		and: "Executor is passing 10 new connections to consumer, each lasts 100 ms"
			10 * executor.execute(consumer, newConnection) >> { sleep(oneHundredOfMiliseconds) }
		and: "Should listen once again for a new connection"
			1 * socket.listenForANewConnection() >> present(newConnection)
		and: "The last found connection will be passed to executor and it will throw exception for purposes of finishing this test"
			1 * executor.execute(consumer, newConnection) >> { throw new RuntimeException() }
		and:
			thrown(RuntimeException)
		and:
			listener.state == STOPPED
	}

	def 'Listener can start listening on socket and handles all new connections until it is not terminated'() {
		given:
			def oneHundredOfMiliseconds = 100
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = Mock()
			def NewConnection newConnection = Mock()
			def ServerState actualServerStateAfterStopping = null;

		when:
			listener.listenAndPassNewConnections(socket)

		then: "Should listen 10 times for new connections"
			10 * socket.listenForANewConnection() >> present(newConnection)
		and: "Executor is passing 10 new connections to consumer, each lasts 100 ms"
			10 * executor.execute(consumer, newConnection) >> { sleep(oneHundredOfMiliseconds) }
		and: "Should listen once again for a new connection. During that time listener is being stopped."
			1 * socket.listenForANewConnection() >> { listener.stopListening();
				actualServerStateAfterStopping = listener.state;
				empty()
			}
		and:
			actualServerStateAfterStopping == STOPPING
			listener.state == STOPPED
	}

	def 'Listener in STOPPED state can start listening and it lasts in RUNNING state until it is terminated with STOPPING state by stopListener to reach STOPPED state'() {
		given:
			def oneHundredOfMiliseconds = 100
			def ConsumerExecutor executor = Stub()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = Mock()
			def stopSuccessful;
			def stateAfterCallingStopListening;
		and:
			executor.execute(_) >> { sleep(oneHundredOfMiliseconds) }

		expect:
			listener.state == STOPPED

		when:
			Thread.start {
				listener.listenAndPassNewConnections(socket)
			}
		and:
			sleep(10 * oneHundredOfMiliseconds)
			stopSuccessful = listener.stopListening()
			stateAfterCallingStopListening = listener.state

		then:
			conditions.within(0.5) {
				listener.state == RUNNING
			}
		and:
			conditions.within(2) {
				listener.state == STOPPING
			}
		and:
			conditions.eventually {
				stopSuccessful == true
				stateAfterCallingStopListening == STOPPED
			}
	}

	@Unroll("Listener cannot be started many times when running no matter which socket (#secondSocket) is passed 2nd time")
	def 'Listener cannot be started many times when running no matter which socket is passed 2nd time'() {
		given:
			def oneHundredOfMiliseconds = 100
			def oneSecondInMiliseconds = 10 * oneHundredOfMiliseconds
		and:
			def ConsumerExecutor executor = Stub()
			def HttpConnectionConsumer consumer = Mock()
		and:
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)
			def ListeningSocket socket = Mock()
		and:
			socket.listenForANewConnection() >> present(Mock(NewConnection))
		and:
			executor.execute(_) >> { sleep(5 * oneHundredOfMiliseconds) }
		and:
			def stopSuccessful
		and:
			def listenerStateRightAfterCallingListenSecondTime

		expect:
			listener.state == STOPPED

		when:
			Thread.start {
				listener.listenAndPassNewConnections(socket)
			}
		and:
			Thread.start {
				sleep(oneSecondInMiliseconds)
				try {
					listener.listenAndPassNewConnections(secondSocket)
				} catch(Exception) {}
				listenerStateRightAfterCallingListenSecondTime = listener.state
			}
		and:
			sleep(2 * oneSecondInMiliseconds)
			stopSuccessful = listener.stopListening()

		then:
			conditions.within(2) {
				listener.state == RUNNING
			}
		and:
			conditions.within(3) {
				listener.state == STOPPING
			}
		and:
			conditions.eventually {
				listener.state == STOPPED
				stopSuccessful == true
			}
		and:
			listenerStateRightAfterCallingListenSecondTime == RUNNING

		where:
			secondSocket << [Mock(ListeningSocket), null]

	}

	def 'Listener cannot be changed to any other state than STOPPED mode when given socket is null'() {
		given:
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = Spy(SynchronousStoppableHttpConnectionListener, constructorArgs: [executor, consumer])

		when:
			listener.listenAndPassNewConnections(null)

		then:
			0 * listener.listenAndPassConnectionsUntilPossibleAndResetFinallyToStoppedState(_)
		and:
			thrown(NullPointerException)
	}

	def 'stopListener cannot change state from STOPPED to STOPPING'() {
		given:
			def ConsumerExecutor executor = Mock()
			def HttpConnectionConsumer consumer = Mock()
			def listener = new SynchronousStoppableHttpConnectionListener(executor, consumer)

		expect:
			listener.state == STOPPED

		when:
			listener.stopListening()

		then:
			listener.state != STOPPING
			1 * executor.terminate()
	}

}
