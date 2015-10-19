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

package io.github.kitarek.elasthttpd.server.networking;

/**
 * Control how quick it will be able bind to socket which was used previously for other server connection. The quick
 * option (REUSE) can lead to deliver of some packets from the previous connection. The slow and stable policy
 * won't allow to bind a socket in TIME_WAIT state and bind operation need to be repeated
 */
public enum AddressAndPortReusePolicy {
	/**
	 * Packets may be sent to a newly bind socket - the leftover packets could not be delivered yet.
	 */
	REUSE_PORT_AND_IP_ADDRESS_RISKING_DELIVERY_OF_OLD_PACKETS,
	/**
	 * Socket will be bound only when there is no chance of the non-delivered leftover packets
	 */
	DO_NOT_USE_PORT_AND_IP_ADDRESS_IF_SOME_PACKETS_WERE_NOT_DELIVERED_YET
}
