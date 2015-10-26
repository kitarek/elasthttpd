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

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * Consume every request that is basically validated and fetched in form of object with possibility to define or
 * redefine a response for it.
 */
public interface HttpRequestConsumer {

	/**
	 * Consume and process fully fetched request from client
	 *
	 * @param request The HTTP request for which consumer should provide response
	 * @param response The initially defined HTTP response object that should be used for generating response
	 */
	void consumeRequest(HttpRequest request, HttpResponse response);
}
