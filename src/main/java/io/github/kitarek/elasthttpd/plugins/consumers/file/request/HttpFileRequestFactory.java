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

/**
 * Create HTTP file request objects.
 */
public class HttpFileRequestFactory {

	private final UriToFileMapper mapper;

	/**
	 * Create the instance of mapper thay will map to resources only under the specified root directory path.
	 *
	 * @param rootDirectoryPath not null path to local directory
	 */
	public HttpFileRequestFactory(final String rootDirectoryPath) {
		this.mapper = new UriToFileMapper(notNull(rootDirectoryPath, "Root directory path cannot be null"));
	}

	/**
	 * Create new file request object based on new HTTP request and response.
	 *
	 * @param request not null
	 * @param response not null
	 * @return not null
	 */
	public HttpFileRequest createNew(HttpRequest request, HttpResponse response) {
		return new ImmutableHttpFileRequest(notNull(request, "HTTP request cannot be null"),
				notNull(response, "HTTP response cannot be null"), mapper);
	}

}
