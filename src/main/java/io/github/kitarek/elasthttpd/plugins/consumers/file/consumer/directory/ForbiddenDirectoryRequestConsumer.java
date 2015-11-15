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

package io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory;


import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;

import java.io.File;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

public class ForbiddenDirectoryRequestConsumer implements HttpDirectoryRequestConsumer {

	private final TemplatedHttpResponder templatedHttpResponder;

	public ForbiddenDirectoryRequestConsumer(TemplatedHttpResponder templatedHttpResponder) {
		this.templatedHttpResponder = notNull(templatedHttpResponder,
				"TemplatedHttpResponder constructor argument needs to be not null");
	}
	public void serveExistingDirectoryElement(HttpFileRequest fileRequest, File requestedDirectory) {
		notNull(fileRequest, "HTTP file request cannot be null");
		notNull(requestedDirectory, "Requested directory file object cannot be null");
		templatedHttpResponder.respondWithResourceForbidden(fileRequest.response(),
				format("403: The requested resource is forbidden (i.e. directory) and cannot be fetched: %s",
						getRequestedUri(fileRequest)));
	}

	private String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}
}
