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

import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;

import java.io.File;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

class HttpFileReadRequestConsumer implements HttpFileRequestConsumer {

	private final HttpFileProducer httpFileProducer;

	public HttpFileReadRequestConsumer(HttpFileProducer httpFileProducer) {
		this.httpFileProducer = notNull(httpFileProducer, "HttpFileProducer constructor argument needs to be not null");
	}

	public void consumeFileRequest(HttpFileRequest fileRequest) {
		final String uri = getRequestedUri(fileRequest);
		final String absolutePath = mapUriToLocalPath(fileRequest, uri);
		doWithRequestedFile(fileRequest, new File(absolutePath));
	}

	private void doWithRequestedFile(HttpFileRequest fileRequest, File requestedFile) {
		if (requestedFile.exists())
			serveExistingFilesystemElement(fileRequest, requestedFile);
		else
			respondThatFileHasNotBeenFound(fileRequest);
	}

	private void serveExistingFilesystemElement(HttpFileRequest fileRequest, File requestedFile) {
		if (requestedFile.isDirectory())
			respondThatFileHasBeenForbiddenToSend(fileRequest);
		else
			httpFileProducer.sendFileOverHttpResponse(requestedFile, fileRequest.response());
	}

	private void respondThatFileHasNotBeenFound(HttpFileRequest fileRequest) {
		fileRequest.response().setStatusCode(SC_NOT_FOUND);
		fileRequest.response().setReasonPhrase("NOT FOUND");
	}

	private void respondThatFileHasBeenForbiddenToSend(HttpFileRequest fileRequest) {
		fileRequest.response().setStatusCode(SC_FORBIDDEN);
		fileRequest.response().setReasonPhrase("FORBIDDEN");
	}

	public String mapUriToLocalPath(HttpFileRequest fileRequest, String uri) {
		return fileRequest.mapper().mapUriRequestPath(uri);
	}

	private String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}
}
