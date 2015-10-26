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
import spock.lang.Specification
import spock.lang.Unroll

import static io.github.kitarek.elasthttpd.commons.Optional.empty
import static io.github.kitarek.elasthttpd.commons.Optional.present
import static io.github.kitarek.elasthttpd.server.networking.KeepAliveMode.ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.KeepAliveMode.INACTIVE_DONT_SEND_KEEP_ALIVE_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.SmallerPacketsSendingPolicy.SEND_SMALLER_PACKETS_INSTANTLY
import static io.github.kitarek.elasthttpd.server.networking.SmallerPacketsSendingPolicy.SEND_SMALLER_PACKETS_LATER_WHEN_OPTIMAL_CONDITIONS

class HttpNewConnectionSpec extends Specification {

	def 'New connection cannot be created without accepted client socket and socket configuration'() {
		when:
			new HttpNewConnection(null, null)

		then:
			thrown(NullPointerException)
	}

	def 'New connection cannot be created without accepted client socket or socket configuration'() {
		when:
			new HttpNewConnection(clientSocket, socketConfiguration)

		then:
			thrown(NullPointerException)

		where:
			clientSocket | socketConfiguration
			null         | Mock(SocketConfiguration)
			Mock(Socket) | null
	}

	@Unroll
	def 'New connection can be created with accepted client socket and socket configuration'() {
		given:
			def clientSocket = Mock(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> socketReceiveBufferSize
			socketConfiguration.getSocketSendBufferSizeInBytes() >> socketSendBufferSize
			socketConfiguration.getKeepAlivePacketsMode() >> keepAliveMode
			socketConfiguration.getSocketTimeoutInMiliseconds() >> timeout
			socketConfiguration.getSmallerPacketsSendingPolicy() >> smallerPacketsSendingPolicy

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)

		then:
			notThrown()

		when:
			def httpServerConnection = newConnection.acceptAndConfigure()

		then:
			httpServerConnection != null
		and:
			setReceiveBufferSizeCalls * clientSocket.setReceiveBufferSize(setReceiveBufferSizeArg)
			setSendBufferSizeCalls * clientSocket.setSendBufferSize(setSendBufferSizeArgs)
			setKeepAliveCalls * clientSocket.setKeepAlive(setKeepAliveArgs)
			setSoTimeoutCalls * clientSocket.setSoTimeout(setSoTimeoutArgs)
			setTcpNoDelayCalls * clientSocket.setTcpNoDelay(setTcpNoDelayArgs)

		where:
			socketReceiveBufferSize << [present(1600), present(2400), empty()]
			setReceiveBufferSizeCalls << [1, 1, 0]
			setReceiveBufferSizeArg << [1600, 2400, _]

			socketSendBufferSize << [present(2400), present(4800), empty()]
			setSendBufferSizeCalls << [1, 1, 0]
			setSendBufferSizeArgs << [2400, 4800, _]

			timeout << [present(10000), present(15000), empty()]
			setSoTimeoutCalls << [1, 1, 0]
			setSoTimeoutArgs << [10000, 15000, _]

			keepAliveMode << [present(INACTIVE_DONT_SEND_KEEP_ALIVE_PACKETS), present(ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS), empty()]
			setKeepAliveCalls << [1, 1, 0]
			setKeepAliveArgs << [false, true, _]

			smallerPacketsSendingPolicy << [present(SEND_SMALLER_PACKETS_INSTANTLY), present(SEND_SMALLER_PACKETS_LATER_WHEN_OPTIMAL_CONDITIONS), empty()]
			setTcpNoDelayCalls << [1, 1, 0]
			setTcpNoDelayArgs << [true, false, _]
	}


	def 'New connection cannot be created resulting in IllegalStateException as client socket throws SocketException for setting keep alive'() {
		given:
		def clientSocket = Stub(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> empty()
			socketConfiguration.getSocketSendBufferSizeInBytes() >> empty()
			socketConfiguration.getKeepAlivePacketsMode() >> present(ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS)
			socketConfiguration.getSocketTimeoutInMiliseconds() >> empty()
			socketConfiguration.getSmallerPacketsSendingPolicy() >> empty()
		and:
			clientSocket.setKeepAlive(_) >> { throw new SocketException() }

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)
			newConnection.acceptAndConfigure()

		then:
			thrown(IllegalStateException)
	}

	def 'New connection cannot be created resulting in IllegalStateException as client socket throws SocketException for setting receive buffer size'() {
		given:
			def clientSocket = Stub(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> present(2048000)
			socketConfiguration.getSocketSendBufferSizeInBytes() >> empty()
			socketConfiguration.getKeepAlivePacketsMode() >> empty()
			socketConfiguration.getSocketTimeoutInMiliseconds() >> empty()
			socketConfiguration.getSmallerPacketsSendingPolicy() >> empty()
		and:
			clientSocket.setReceiveBufferSize(_) >> { throw new SocketException() }

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)
			newConnection.acceptAndConfigure()

		then:
			thrown(IllegalStateException)
	}

	def 'New connection cannot be created resulting in IllegalStateException as client socket throws SocketException for setting send buffer size'() {
		given:
			def clientSocket = Stub(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> empty()
			socketConfiguration.getSocketSendBufferSizeInBytes() >> present(1024000)
			socketConfiguration.getKeepAlivePacketsMode() >> empty()
			socketConfiguration.getSocketTimeoutInMiliseconds() >> empty()
			socketConfiguration.getSmallerPacketsSendingPolicy() >> empty()
		and:
			clientSocket.setSendBufferSize(_) >> { throw new SocketException() }

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)
			newConnection.acceptAndConfigure()

		then:
			thrown(IllegalStateException)
	}

	def 'New connection cannot be created resulting in IllegalStateException as client socket throws SocketException for setting timeout'() {
		given:
			def clientSocket = Stub(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> empty()
			socketConfiguration.getSocketSendBufferSizeInBytes() >> empty()
			socketConfiguration.getKeepAlivePacketsMode() >> empty()
			socketConfiguration.getSocketTimeoutInMiliseconds() >> present(1000)
			socketConfiguration.getSmallerPacketsSendingPolicy() >> empty()
		and:
			clientSocket.setSoTimeout(_) >> { throw new SocketException() }

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)
			newConnection.acceptAndConfigure()

		then:
			thrown(IllegalStateException)
	}

	def 'New connection cannot be created resulting in IllegalStateException as client socket throws SocketException for setting TCP no delay'() {
		given:
			def clientSocket = Stub(Socket)
		and:
			def socketConfiguration = Stub(SocketConfiguration)
			socketConfiguration.getSocketReceiveBufferSizeInBytes() >> empty()
			socketConfiguration.getSocketSendBufferSizeInBytes() >> empty()
			socketConfiguration.getKeepAlivePacketsMode() >> empty()
			socketConfiguration.getSocketTimeoutInMiliseconds() >> empty()
			socketConfiguration.getSmallerPacketsSendingPolicy() >> present(SEND_SMALLER_PACKETS_INSTANTLY)
		and:
			clientSocket.setTcpNoDelay(_) >> { throw new SocketException() }

		when:
			def newConnection = new HttpNewConnection(clientSocket, socketConfiguration)
			newConnection.acceptAndConfigure()

		then:
			thrown(IllegalStateException)
	}
}