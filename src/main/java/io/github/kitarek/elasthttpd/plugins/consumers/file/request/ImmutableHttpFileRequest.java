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

package io.github.kitarek.elasthttpd.plugins.consumers.file.request;


import io.github.kitarek.elasthttpd.plugins.consumers.file.mapper.UriToFileMapper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import static org.apache.commons.lang3.Validate.notNull;

public class ImmutableHttpFileRequest implements HttpFileRequest {

	private final HttpRequest request;
	private final UriToFileMapper mapper;
	private final HttpResponse response;

	public ImmutableHttpFileRequest(HttpRequest request, HttpResponse response, UriToFileMapper uriToFileMapper) {
		this.request = notNull(request, "HTTP request cannot be null");
		this.response = notNull(response, "HTTP response cannot be null");
		this.mapper = notNull(uriToFileMapper, "URI to File mapper cannot be null");
	}

	public HttpRequest request() {
		return request;
	}

	public HttpResponse response() {
		return response;
	}

	public UriToFileMapper mapper() {
		return mapper;
	}
}
