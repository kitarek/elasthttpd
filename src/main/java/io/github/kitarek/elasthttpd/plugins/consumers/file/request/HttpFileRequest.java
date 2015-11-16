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

/**
 * Represents requests directed to FileServer plugin
 */
public interface HttpFileRequest {

	/**
	 * Original HTTP request of this file request
	 *
	 * @return not null
	 */
	HttpRequest request();

	/**
	 * Original HTTP response of this file request
	 * @return not null
	 */
	HttpResponse response();

	/**
	 * Mapper to determine the exact local file object pointed by i.e. HTTP request URL.
	 *
	 * @return not null
	 */
	UriToFileMapper mapper();
}
