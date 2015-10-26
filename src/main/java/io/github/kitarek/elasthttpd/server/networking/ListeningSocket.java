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

import io.github.kitarek.elasthttpd.commons.Optional;

/**
 * Simplified representation of server socket.
 */
public interface ListeningSocket {

	/**
	 * Listens for a new connection. If new connection is present to it can be serviced. Otherwise if new connection
	 * is not present then the socket is no more listening for a new connections or no new connection has been catched
	 * before the socket was closed.
	 *
	 * @return The not-null optional reference to interface for operating on a new connection
	 */
	Optional<NewConnection> listenForANewConnection();

	/**
	 * Closes listening socket so it cannot be used anymore.
	 */
	void stopListening();
}
