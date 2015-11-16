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
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequest;

import java.io.File;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Strategy that allows for directories to serve specified resource that is always located relatively under
 * the directory resource requested by HTTP client.
 *
 * The current implementation does not enforce server base path limitation so please be aware if you use '..'
 * as subresource.
 */
public class DirectorySubResourceRequestConsumer implements HttpDirectoryRequestConsumer {

	private final HttpFileProducer producer;
	private final TemplatedHttpResponder responder;
	private final String subResourceToServe;

	/**
	 * Create strategy with the following dependencies
	 *
	 * @param producer not-null
	 * @param responder not-null
	 * @param subResourceToServe not-null string that represents either one level in resource path or
	 *                           part of resource path that will be appended to the requested resource directory
	 *                           (collection). The latter requires to separate each level with request path level
	 *                           separator '/'. Usage of '..' special directory symbol is permitted however not
	 *                           recommended.
	 */
	public DirectorySubResourceRequestConsumer(HttpFileProducer producer,
											   TemplatedHttpResponder responder,
											   String subResourceToServe) {
		this.producer = notNull(producer, "HttpFileProducer cannot be null");
		this.responder = notNull(responder, "TemplatedHttpResponder cannot be null");
		this.subResourceToServe = notNull(subResourceToServe, "Subresource identifer cannot be null");
	}

	public void serveExistingDirectoryElement(HttpFileRequest fileRequest, File requestedDirectory) {
		notNull(fileRequest, "HTTP file request cannot be null");
		notNull(requestedDirectory, "Requested directory file object cannot be null");
		File subFile = new File(requestedDirectory, subResourceToServe);
		if (subFile.exists() && subFile.isFile())
			producer.sendFileOverHttpResponse(subFile, fileRequest.response());
		else
			respondThatFileHasNotBeenFound(fileRequest);
	}

	private void respondThatFileHasNotBeenFound(HttpFileRequest fileRequest) {
		responder.respondWithResourceNotFound(fileRequest.response(),
				format("404: The default file resource for requested resource collection was not found: %s",
						getRequestedUri(fileRequest)));
	}

	private String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}

}
