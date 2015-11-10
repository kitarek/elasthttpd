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

package io.github.kitarek.elasthttpd.plugins.consumers.file.producer;

import org.apache.http.HttpResponse;

import java.io.File;

/**
 * Allows to construct HTTP response that will send over HTTP protocol an existing file from local filesystem
 */
public class HttpFileProducer {

	/**
	 * Transform HttpResponse in such way it will contain the specified fle
	 *
	 * @param localFile a valid not null file instance that must exist and be readable
	 * @param response a valid initially preconfigured HTTP response
	 */
	public void sendFileOverHttpResponse(File localFile, HttpResponse response) {

	}
}
