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

package io.github.kitarek.elasthttpd.server.networking
import org.easymock.EasyMock
import org.junit.Rule
import org.powermock.api.easymock.PowerMock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import spock.lang.Specification
import spock.lang.Unroll

import javax.net.ServerSocketFactory

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present
import static io.github.kitarek.elasthttpd.server.networking.AddressAndPortReusePolicy.DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET
import static io.github.kitarek.elasthttpd.server.networking.AddressAndPortReusePolicy.REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.HttpConfiguredServerSocket.newHttpConfiguredServerSocket
import static org.powermock.api.easymock.PowerMock.mockStatic

@PrepareForTest([ServerSocketFactory.class])
class HttpConfiguredServerSocketSpec extends Specification {

	@Rule PowerMockRule powerMockRule = new PowerMockRule();

	def 'HttpConfiguredServerSocket cannot be created its configuration[2]'() {
		when:
			newHttpConfiguredServerSocket(null);

		then:
			thrown(NullPointerException)
	}

	@Unroll
	def 'HttpConfiguredServerSocket can be created with its configuration and socket is configured based on provided configuration'() {
		given:
			def serverSocketFactoryMock = Mock(ServerSocketFactory)
		and:
			mockStatic(ServerSocketFactory)
			EasyMock.expect(ServerSocketFactory.getDefault()).andReturn(serverSocketFactoryMock).once();
			PowerMock.replayAll()
		and:
			def listeningAddress = new InetAddress()
			def listeningPort = 1000
			def connectionsToAcceptQueueSize = 10000
			def receiveBufferSize = 160000
		and:
			def socketConfigurationStub = Stub(SocketConfiguration)
			socketConfigurationStub.getListeningAddress() >> listeningAddress
			socketConfigurationStub.getListeningPort() >> listeningPort
			socketConfigurationStub.getConnectionsToAcceptQueueSize() >> connectionsToAcceptQueueSize
			socketConfigurationStub.getSocketReceiveBufferSizeInBytes() >> present(receiveBufferSize)
			socketConfigurationStub.getAddressAndPortReusePolicy() >> addressAndPortReusePolicy
		and:
			def serverSocketMock = Mock(ServerSocket)

		when:
			newHttpConfiguredServerSocket(socketConfigurationStub);

		then:
			PowerMock.verifyAll()
			1 * serverSocketFactoryMock.createServerSocket(listeningPort, connectionsToAcceptQueueSize, listeningAddress) >> serverSocketMock
		and:
			1 * serverSocketMock.setReceiveBufferSize(receiveBufferSize)
			setReuseAddressNumberOfCalls * serverSocketMock.setReuseAddress(setReuseAddressFlag)
			0 * serverSocketMock._

		where:
			addressAndPortReusePolicy                                                      | setReuseAddressNumberOfCalls | setReuseAddressFlag
			present(REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS)             | 1                            | true
			present(DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET) | 1                            | false
			empty()                                                                        | 0                            | null
	}

	def 'HttpConfiguredServerSocket can be created and can listen accepting and returning a new connection'() {
		given:
			def serverSocketFactoryMock = Mock(ServerSocketFactory)
		and:
			mockStatic(ServerSocketFactory)
			EasyMock.expect(ServerSocketFactory.getDefault()).andReturn(serverSocketFactoryMock).once();
			PowerMock.replayAll()
		and:
			def socketConfigurationMock = Mock(SocketConfiguration)
			socketConfigurationMock.getAddressAndPortReusePolicy() >> empty()
			socketConfigurationMock.getSocketReceiveBufferSizeInBytes() >> empty()
		and:
			def serverSocketMock = Mock(ServerSocket)
			serverSocketFactoryMock.createServerSocket(_, _, _) >> serverSocketMock
		and:
			def clientSocket = Mock(Socket)

		when:
			def ListeningSocket listeningSocket = newHttpConfiguredServerSocket(socketConfigurationMock);
		and:
			def newConnection = listeningSocket.listenForANewConnection()

		then:
			1 * serverSocketMock.accept() >> clientSocket
		and:
			newConnection != null
	}

}
