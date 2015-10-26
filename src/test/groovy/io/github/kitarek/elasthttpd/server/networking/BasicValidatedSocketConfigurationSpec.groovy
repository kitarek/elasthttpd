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

import static io.github.kitarek.elasthttpd.server.networking.AddressAndPortReusePolicy.DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET
import static io.github.kitarek.elasthttpd.server.networking.AddressAndPortReusePolicy.REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.KeepAliveMode.ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.KeepAliveMode.INACTIVE_DONT_SEND_KEEP_ALIVE_PACKETS
import static io.github.kitarek.elasthttpd.server.networking.SmallerPacketsSendingPolicy.SEND_SMALLER_PACKETS_INSTANTLY
import static io.github.kitarek.elasthttpd.server.networking.SmallerPacketsSendingPolicy.SEND_SMALLER_PACKETS_LATER_WHEN_OPTIMAL_CONDITIONS

class BasicValidatedSocketConfigurationSpec extends Specification {

	def 'Cannot create socket configuration providing nulls or zeros'() {
		when:
			new BasicValidatedSocketConfiguration(null, portNumber)

		then:
			thrown(NullPointerException)

		where:
			portNumber << [0, 1, 2 ,3]
	}

	def 'Cannot create socket configuration providing negative or zero port number'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), portNumber)

		then:
			thrown(IllegalArgumentException)

		where:
			portNumber << [-43543344, -1, 0]
	}

	def 'Cannot create socket configuration providing port number higher than 65535'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), portNumber)

		then:
			thrown(IllegalArgumentException)

		where:
			portNumber << [65536, 65537, 23432432, 324432434]
	}

	def 'Can create socket configuration providing port number greater than 0 and lower than 65536'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), portNumber)

		then:
			notThrown()

		where:
			portNumber << [1, 100, 1024, 32434]
	}

	def 'Cannot create socket configuration providing queue size lower than 1'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, queueSize)

		then:
			thrown(IllegalArgumentException)

		where:
			queueSize << [0, -1, -32432]
	}

	def 'Can create socket configuration providing queue size and portNumber higher than 0'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, queueSize)

		then:
			notThrown()

		where:
			queueSize << [1, 2, 132434]
	}

	def 'Can create socket configuration providing queue size and portNumber higher than 0 and any other setting as null which means system default will be chosen'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, queueSize, null, null, null, null, null, null);

		then:
			notThrown()

		where:
			queueSize << [1, 2, 132434]
	}

	@Unroll
	def 'Cannot create socket configuration providing correct queue size and portNumber, but not receive buffer size'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, bufferSize,
					null, null, null, null, null);

		then:
			thrown(IllegalArgumentException)

		where:
			bufferSize << [0, -1 ,-100]
	}

	@Unroll
	def 'Cannot create socket configuration providing correct queue size and portNumber, but not send buffer size'() {
		when:
		new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, null, bufferSize, null, null, null, null);

		then:
			thrown(IllegalArgumentException)

		where:
			bufferSize << [0, -1 ,-100]
	}

	@Unroll
	def 'Can create socket configuration providing correct queue size and portNumber, but not receive buffer size'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, bufferSize,
				null, null, null, null, null);

		then:
			notThrown()

		where:
			bufferSize << [1, 2 ,100]
	}

	@Unroll
	def 'Can create socket configuration providing correct queue size and portNumber, but not send buffer size'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, null, bufferSize, null, null, null, null);

		then:
			notThrown()

		where:
			bufferSize << [1, 2, 1000]
	}

	@Unroll
	def 'Cannot create socket configuration providing correct queue size and portNumber, but not receive/send buffer sizes'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, bufferSize, bufferSize, null, null, null, null);

		then:
			thrown(IllegalArgumentException)

		where:
			bufferSize << [0, -1 ,-100]
	}

	@Unroll
	def 'Cannot create socket configuration providing correct queue size and portNumber, but not timeout'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, null, null, null, null, timeout, null);

		then:
		thrown(IllegalArgumentException)

		where:
			timeout << [0, -1 ,-100]
	}

	@Unroll
	def 'Cannot create socket configuration providing correct queue size and portNumber, but not receive/send buffers and timeout'() {
		when:
			new BasicValidatedSocketConfiguration(new InetAddress(), 1, 1, bufferSize, bufferSize, null, null, timeout, null);

		then:
			thrown(IllegalArgumentException)

		where:
			bufferSize << [0, -1 ,-100]
			timeout << [0, -1 ,-100]
	}

	@Unroll
	def 'Socket configuration can be used'() {
		given:
			def address = new InetAddress()
		when:
			def SocketConfiguration c = new BasicValidatedSocketConfiguration(address, 1, 1, bufferSize,
					bufferSize, ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS, SEND_SMALLER_PACKETS_INSTANTLY, timeout,
					DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET);

		then:
			c.connectionsToAcceptQueueSize == 1
			c.listeningPort == 1
			c.listeningAddress == address
		and:
			c.keepAlivePacketsMode.present
			c.keepAlivePacketsMode.get() == ACTIVE_AND_SEND_KEEP_ALIVE_PACKETS
		and:
			c.socketTimeoutInMiliseconds.present
			c.socketTimeoutInMiliseconds.get() == timeout
		and:
			c.socketReceiveBufferSizeInBytes.present
			c.socketReceiveBufferSizeInBytes.get() == bufferSize
		and:
			c.socketSendBufferSizeInBytes.present
			c.socketSendBufferSizeInBytes.get() == bufferSize
		and:
			c.smallerPacketsSendingPolicy.present
			c.smallerPacketsSendingPolicy.get() == SEND_SMALLER_PACKETS_INSTANTLY
		and:
			c.addressAndPortReusePolicy.present
			c.addressAndPortReusePolicy.get() == DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET

		where:
			bufferSize << [1, 100, 200]
			timeout << [1, 250, 1000]
	}

	@Unroll
	def 'Socket configuration can be used with some optional enum values'() {
		given:
			def address = new InetAddress()
		when:
			def SocketConfiguration c = new BasicValidatedSocketConfiguration(address, 1, 1, bufferSize,
				bufferSize, null, null, timeout, null);

		then:
			c.connectionsToAcceptQueueSize == 1
			c.listeningPort == 1
			c.listeningAddress == address
		and:
			c.keepAlivePacketsMode.notPresent
		and:
			c.socketTimeoutInMiliseconds.present
			c.socketTimeoutInMiliseconds.get() == timeout
		and:
			c.socketReceiveBufferSizeInBytes.present
			c.socketReceiveBufferSizeInBytes.get() == bufferSize
		and:
			c.socketSendBufferSizeInBytes.present
			c.socketSendBufferSizeInBytes.get() == bufferSize
		and:
			c.smallerPacketsSendingPolicy.notPresent
		and:
			c.addressAndPortReusePolicy.notPresent

		where:
			bufferSize << [1, 100, 200]
			timeout << [1, 250, 1000]
	}


	def 'Socket configuration can be used with some optional numeric values'() {
		given:
			def address = new InetAddress()
		when:
			def SocketConfiguration c = new BasicValidatedSocketConfiguration(address, 1, 1, null,
				null, INACTIVE_DONT_SEND_KEEP_ALIVE_PACKETS, SEND_SMALLER_PACKETS_LATER_WHEN_OPTIMAL_CONDITIONS,
					null, REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS);

		then:
			c.connectionsToAcceptQueueSize == 1
			c.listeningPort == 1
			c.listeningAddress == address
		and:
			c.keepAlivePacketsMode.present
			c.keepAlivePacketsMode.get() == INACTIVE_DONT_SEND_KEEP_ALIVE_PACKETS
		and:
			c.socketTimeoutInMiliseconds.notPresent
		and:
			c.socketReceiveBufferSizeInBytes.notPresent
		and:
			c.socketSendBufferSizeInBytes.notPresent
		and:
			c.smallerPacketsSendingPolicy.present
			c.smallerPacketsSendingPolicy.get() == SEND_SMALLER_PACKETS_LATER_WHEN_OPTIMAL_CONDITIONS
		and:
			c.addressAndPortReusePolicy.present
			c.addressAndPortReusePolicy.get() == REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS
	}

}
