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

import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;

import java.io.File;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Request consumer that deletes local file mapped by specified URI.
 */
public class HttpFileDeleteRequestConsumer implements HttpFileRequestConsumer {

	private final TemplatedHttpResponder templatedHttpResponder;

	public HttpFileDeleteRequestConsumer(TemplatedHttpResponder templatedHttpResponder) {
		this.templatedHttpResponder = notNull(templatedHttpResponder, "Templated HTTP responder cannot be null");
	}

	public void consumeFileRequest(HttpFileRequest fileRequest) {
		notNull(fileRequest, "File request cannot be null");
		final String uri = getRequestedUri(fileRequest);
		final String absoluteLocalPathToResource = mapUriToLocalPath(fileRequest, uri);
		doWithRequestedFile(fileRequest, new File(absoluteLocalPathToResource));
	}

	private void doWithRequestedFile(HttpFileRequest fileRequest, File file) {
		if (file.exists())
			doWithExistingRequestedFileResource(fileRequest, file);
		else
			respondThatResourceIsNotFound(fileRequest);
	}

	private void doWithExistingRequestedFileResource(HttpFileRequest fileRequest, File file) {
		if (file.isDirectory())
			respondThatResourceIsForbidden(fileRequest);
		else
			tryDeleteFileAndRespondToRequest(fileRequest, file);
	}

	void tryDeleteFileAndRespondToRequest(HttpFileRequest fileRequest, File file) {
		if (file.delete())
			respondThatResourceIsDeleted(fileRequest);
		else
			respondThatFileWasNotDeleted(fileRequest);
	}

	private void respondThatFileWasNotDeleted(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithInternalServerError(fileRequest.response(),
				format("Server wasn't able to fulfil delete request on resource: %s", getRequestedUri(fileRequest)));
	}

	private void respondThatResourceIsDeleted(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithNoContentAndReasonDeleted(fileRequest.response());
	}

	private void respondThatResourceIsForbidden(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithResourceForbidden(fileRequest.response(),
				format("The following resource is forbidden: %s", getRequestedUri(fileRequest)));
	}

	private void respondThatResourceIsNotFound(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithResourceNotFound(fileRequest.response(),
				format("Resource cannot be found: %s", getRequestedUri(fileRequest)));
	}

	private String mapUriToLocalPath(HttpFileRequest fileRequest, String uri) {
		return fileRequest.mapper().mapUriRequestPath(uri);
	}

	private String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}

}
