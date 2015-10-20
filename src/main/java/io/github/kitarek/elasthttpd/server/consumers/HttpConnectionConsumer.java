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

package io.github.kitarek.elasthttpd.server.consumers;

import io.github.kitarek.elasthttpd.server.networking.NewConnection;

/**
 * Handles new HTTP client connection from the very begining of its lifetime till the very last message. Responsible
 * for handling one client connected to HTTP server.
 *
 * The most common pattern is that this consumer is run per client connection in a separate thread.
 *
 * This class shouldn't handle and process directly particular requests. However it might prepare new HTTP requests
 * for such processing.
 */
public interface HttpConnectionConsumer {
	/**
	 * Consumes and handles new client connection received in HTTP server.
	 *
	 * @param c new connection to be handled (always not null) by this consumer
	 */
	void consumeConnection(NewConnection c);
}
