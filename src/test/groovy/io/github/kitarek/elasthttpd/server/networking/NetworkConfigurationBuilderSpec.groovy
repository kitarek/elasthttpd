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

class NetworkConfigurationBuilderSpec extends Specification {

	def 'Builder can be created via static factory method without specyfing any parameters'() {
		when:
			def networkConfigurationBuilder  = NetworkConfigurationBuilder.newConfiguration()

		then:
			networkConfigurationBuilder != null
		and:
			notThrown()
	}

	def 'Builder can create default socket configuration with binding to localhost:18181 without specifying any details'() {
		when:
			def cfg = NetworkConfigurationBuilder.newConfiguration().createNow();

		then:
			cfg.connectionsToAcceptQueueSize == 1024
			cfg.listeningAddress == Inet4Address.getByName("localhost")
			cfg.listeningPort == 18181
			cfg.addressAndPortReusePolicy.isNotPresent()
			cfg.keepAlivePacketsMode.isNotPresent()
			cfg.smallerPacketsSendingPolicy.isNotPresent()
			cfg.socketReceiveBufferSizeInBytes.isNotPresent()
			cfg.socketSendBufferSizeInBytes.isNotPresent()
			cfg.socketTimeoutInMiliseconds.isNotPresent()
	}


	def 'Builder must not accept null Internet Address'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setListeningAddress(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll("Builder must not accept negative port: #tooLowPortNumber")
	def 'Builder must not accept negative port'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setListeningPort(tooLowPortNumber)

		then:
			thrown(IllegalArgumentException)

		where:
			tooLowPortNumber << [-1, -100, -300]
	}

	@Unroll("Builder must not accept port number #tooHighPortNumber which is higher than 65535")
	def 'Builder must not accept port number higher than 65535'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setListeningPort(tooHighPortNumber)

		then:
			thrown(IllegalArgumentException)

		where:
			tooHighPortNumber << [65536, 65537, 6553789]
	}

	@Unroll('Builder must not accept negative queue size: #tooLowPortNumber')
	def 'Builder must not accept negative queue size'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setConnectionsToAcceptQueueSize(tooLowPortNumber)

		then:
			thrown(IllegalArgumentException)

		where:
			tooLowPortNumber << [-1, -100, -100000]
	}

	def 'Builder must not accept null for AddressAndPortReusePolicy'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setAddressAndPortReusePolicy(null)

		then:
			thrown(NullPointerException)
	}

	def 'Builder must not accept null for SmallerPacketsSendingPolicy'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setSmallerPacketsSendingPolicy(null)

		then:
			thrown(NullPointerException)
	}

	def 'Builder must not accept null for KeepAlivePacketsMode'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setKeepAlivePacketsMode(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll("Builder must not accept #tooLowBufferSizeInBytes for socket receive buffer size")
	def 'Builder must not accept negative value for socket receive buffer size'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setSocketReceiveBufferSizeInBytes(tooLowBufferSizeInBytes)

		then:
			thrown(IllegalArgumentException)

		where:
			tooLowBufferSizeInBytes << [0, -1, -100, -100000]
	}

	def 'Builder must not accept null value for socket receive buffer size'() {
		when:
		NetworkConfigurationBuilder.newConfiguration().setSocketReceiveBufferSizeInBytes(null)

		then:
		thrown(NullPointerException)
	}

	def 'Builder must not accept null for socket send buffer size'() {
		when:
		NetworkConfigurationBuilder.newConfiguration().setSocketSendBufferSizeInBytes(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll("Builder must not accept #tooLowBufferSizeInBytes for socket send buffer size")
	def 'Builder must not accept negative value for socket send buffer size'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setSocketSendBufferSizeInBytes(tooLowBufferSizeInBytes)

		then:
			thrown(IllegalArgumentException)

		where:
			tooLowBufferSizeInBytes << [0, -1, -100, -100000]
	}

	def 'Builder must not accept null for socket timeout value'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setSocketTimeoutInMiliseconds(null)

		then:
			thrown(NullPointerException)
	}

	@Unroll("Builder must not accept #tooLowTimeoutInMiliseconds for socket timeout in miliseconds")
	def 'Builder must not accept negative value for socket timeout in miliseconds'() {
		when:
			NetworkConfigurationBuilder.newConfiguration().setSocketTimeoutInMiliseconds(tooLowTimeoutInMiliseconds)

		then:
			thrown(IllegalArgumentException)

		where:
			tooLowTimeoutInMiliseconds << [0, -1, -100, -100000]
	}


	def 'Builder can create custom socket configuration'() {
		given:
			def expectedTimeout = 1024
			def expectedSendBufferSize = 1024000
			def expectedReceiveBufferSize = 2048000
			def expectedConnectionsToAcceptQueueSize = 2048
			def expectedListeningPort = 18887
			def byte[] inetAddressByteArray = [10, 0, 0, 1];
			def expectedListeningAddress = Inet4Address.getByAddress(inetAddressByteArray)
			def expectedKeepAlivePacketsMode = KeepAliveMode.ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS
			def expectedSmallerPacketsSendingPolicy = SmallerPacketsSendingPolicy.SEND_SMALLER_PACKETS_INSTANTLY
			def expectedAddressAndPortReusePolicy = AddressAndPortReusePolicy.REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS

		when:
			def cfg = NetworkConfigurationBuilder
					.newConfiguration()
					.setSocketTimeoutInMiliseconds(expectedTimeout)
					.setKeepAlivePacketsMode(expectedKeepAlivePacketsMode)
					.setAddressAndPortReusePolicy(expectedAddressAndPortReusePolicy)
					.setSmallerPacketsSendingPolicy(expectedSmallerPacketsSendingPolicy)
					.setConnectionsToAcceptQueueSize(expectedConnectionsToAcceptQueueSize)
					.setListeningAddress(expectedListeningAddress)
					.setListeningPort(expectedListeningPort)
					.setSocketReceiveBufferSizeInBytes(expectedReceiveBufferSize)
					.setSocketSendBufferSizeInBytes(expectedSendBufferSize)
					.createNow();

		then:
			cfg.connectionsToAcceptQueueSize == expectedConnectionsToAcceptQueueSize
			cfg.listeningAddress == expectedListeningAddress
			cfg.listeningPort == expectedListeningPort
			cfg.addressAndPortReusePolicy.isPresent()
			cfg.addressAndPortReusePolicy.get() == expectedAddressAndPortReusePolicy
			cfg.keepAlivePacketsMode.isPresent()
			cfg.keepAlivePacketsMode.get() == expectedKeepAlivePacketsMode
			cfg.smallerPacketsSendingPolicy.isPresent()
			cfg.smallerPacketsSendingPolicy.get() == expectedSmallerPacketsSendingPolicy
			cfg.socketReceiveBufferSizeInBytes.isPresent()
			cfg.socketReceiveBufferSizeInBytes.get() == expectedReceiveBufferSize
			cfg.socketSendBufferSizeInBytes.isPresent()
			cfg.socketSendBufferSizeInBytes.get() == expectedSendBufferSize
			cfg.socketTimeoutInMiliseconds.isPresent()
			cfg.socketTimeoutInMiliseconds.get() == expectedTimeout

	}
}
