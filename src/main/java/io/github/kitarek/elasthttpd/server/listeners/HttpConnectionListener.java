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

package io.github.kitarek.elasthttpd.server.listeners;

import io.github.kitarek.elasthttpd.model.ServerState;
import io.github.kitarek.elasthttpd.server.networking.ListeningSocket;

/**
 * Listen continuously for new HTTP connections initiated by clients. Passes new connections to another component
 * that is able to handle or consume it.
 *
 * Every implementation of this interface will be blocked on {@link ListeningSocket} so it may require calling this
 * class in separate thread executor.
 *
 * The implementations of this interface should pass newly received connection from {@link ListeningSocket} to
 * dedicated component to which service of that connection can be delegated properly. Please also note that this
 * dedicated component should work on the passed connection asynchronously to not cause blockage of this listener.
 */
public interface HttpConnectionListener {
	/**
	 * Listen and wait for new connections on a medium which is a configured socket in a listening state.
	 *
	 * Please note that the direct caller of this method will be blocked until {@link #stopListening()} method
	 * will be invoked or until any unexpected event or exception will occur.
	 *
	 * The method can be invoked only once at the same time.
	 *
	 * @param medium not-null socket that needs to be configured and usually bound to IP/port.
	 */
	void listenAndPassNewConnections(ListeningSocket medium);

	/**
	 * Stop listening on medium specified by {@link #listenAndPassNewConnections(ListeningSocket)}.
	 *
	 * Please note that it stop operation could not be finalized instantly and it may take some time to finish it.
	 * Please also be informed that the implementations of this class shouldn't do any direct changes to listening
	 * socket.
	 *
	 * @return true if stop has been sucessfully invoked and the listener will be eventually stopped
	 */
	boolean stopListening();

	/**
	 * Inform about listener state method:
	 * <ol>
	 *     <li>{@link ServerState#RUNNING} if {@link #listenAndPassNewConnections(ListeningSocket)} is actively
	 *     running</li>
	 *     <li>{@link ServerState#STOPPED} if {@link #listenAndPassNewConnections(ListeningSocket)} is <b>not</b>
	 *     actively running</li>
	 *     <li>{@link ServerState#STOPPING} if {@link #stopListening()} has been successfully called and
	 *     {@link #listenAndPassNewConnections(ListeningSocket)} during that time was running and it will be
	 *     eventually finished</li>
	 * </ol>
	 * @return current server state
	 */
	ServerState getState();
}
