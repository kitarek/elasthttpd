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

package io.github.kitarek.elasthttpd.plugins.consumers.file.consumer;

import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;

/**
 * Actor that consumes HTTP requests from clients related to file operations (read, write, deletion etc) on server side
 * The implementation of this interface should be thread-safe. The best pattern is not to hold any internal state
 * in the implementation.
 */
public interface HttpFileRequestConsumer {

	/**
	 * Consume and process HTTP request related to file server.
	 * See more {@link HttpFileRequest} for detilas what the file request is.
	 *
	 * @param fileRequest always not null instance of HTTP file request
	 */
	void consumeFileRequest(HttpFileRequest fileRequest);
}
