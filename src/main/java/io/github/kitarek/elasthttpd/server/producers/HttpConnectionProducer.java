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

package io.github.kitarek.elasthttpd.server.producers;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * The responsibility of this producer is to successfully deliver the specified by user HTTP response based
 * on all requirements and limitations to HTTP protocol
 */
public interface HttpConnectionProducer {
	/**
	 * Send response in scope of open and existing serverConnection.
	 *
	 * Please note that for not every requested method the response will look exactly the same. For some requested
	 * methods even if response could be very precise only some part of header could be transferred.
	 * That's why it is required to provide it.
	 *
	 * If you don't provide HTTP method for which you are responding, any implementation should assume that the fully
	 * defined response will be sent.
	 *
	 * @param responseToSend the valid and correct response to be sent
	 * @param httpContext not-null HTTP context that holds differnt HTTP transmission/connection related attributes
	 */
	void sendResponse(HttpResponse responseToSend, HttpContext httpContext);

}
