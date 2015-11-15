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
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

public class HttpFileWriteRequestConsumer implements HttpFileRequestConsumer {

	public static final Logger LOGGER = LoggerFactory.getLogger(HttpFileWriteRequestConsumer.class);
	private final TemplatedHttpResponder templatedHttpResponder;

	public HttpFileWriteRequestConsumer(TemplatedHttpResponder templatedHttpResponder) {
		this.templatedHttpResponder = notNull(templatedHttpResponder, "Templated HTTP responder cannot be null");
	}

	public void consumeFileRequest(HttpFileRequest fileRequest) {
		notNull(fileRequest, "File request cannot be null");
		final String uri = getRequestedUri(fileRequest);
		final String absoluteLocalPathToResource = mapUriToLocalPath(fileRequest, uri);
		doWithRequestedFile(fileRequest, new File(absoluteLocalPathToResource));
	}

	private void doWithRequestedFile(HttpFileRequest fileRequest, File file) {
		if (file.isDirectory())
			respondThatResourceIsForbidden(fileRequest);
		else
			tryToWriteFileAndRespondToRequest(fileRequest, file);
	}

	private void respondThatResourceIsForbidden(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithResourceForbidden(fileRequest.response(),
				format("The following resource is directory and cannot be overwritten as file: %s",
						getRequestedUri(fileRequest)));
	}

	private void tryToWriteFileAndRespondToRequest(HttpFileRequest fileRequest, File file) {
		if (isRequestImplementingEntity(fileRequest.request()))
			decodeRequestEntityAndWriteToFile(fileRequest, file,
					upgradeHttpRequestToSupportEntities(fileRequest.request()));
		else
			createOrUpdateAnEmptyFile(fileRequest, file);
	}

	private void createOrUpdateAnEmptyFile(HttpFileRequest fileRequest, File file) {
		try {
			createOrUpdateAnEmptyFileUnchecked(fileRequest, file);
		} catch (FileNotFoundException e) {
			handleFileNotFoundException(fileRequest, file, e);
		}
	}

	private void handleFileNotFoundException(HttpFileRequest fileRequest, File file, FileNotFoundException e) {
		templatedHttpResponder.respondWithResourceNotFound(fileRequest.response(),
				format("Cannot find the resource or resources in requested path: %s", getRequestedUri(fileRequest)));
		LOGGER.error(format("Cannot find the requested local file '%s' identified by resource: '%s'", file.getAbsolutePath(),
				getRequestedUri(fileRequest)), e);
	}

	private void createOrUpdateAnEmptyFileUnchecked(HttpFileRequest fileRequest, File file) throws FileNotFoundException {
		OutputStream outputStream = new FileOutputStream(file);
		closeTheStream(outputStream, file);
		postprocessCreationOfFile(fileRequest, file);
	}

	void closeTheStream(OutputStream outputStream, File file) {
		try {
			outputStream.close();
		} catch (IOException e) {
			LOGGER.warn(format("An unexpected error when closing the file: %s", file.getAbsolutePath()), e);
		}
	}

	void postprocessCreationOfFile(HttpFileRequest fileRequest, File file) {
		if (file.exists())
			respondThatResourceIsCreated(fileRequest);
		else
			respondWithInternalServerError(fileRequest);
	}

	private void respondWithInternalServerError(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondWithInternalServerError(fileRequest.response(),
				format("There was an unexpected failure when creating the resource: %s", getRequestedUri(fileRequest)));
	}

	private void respondThatResourceIsCreated(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondThatResourceIsCreated(fileRequest.response());
		// TODO ... create an URL
	}

	private void decodeRequestEntityAndWriteToFile(HttpFileRequest fileRequest, File file,
												   HttpEntityEnclosingRequest httpEntityEnclosingRequest) {

	}

	private boolean isRequestImplementingEntity(HttpRequest request) {
		return (request instanceof HttpEntityEnclosingRequest);
	}

	private HttpEntityEnclosingRequest upgradeHttpRequestToSupportEntities(HttpRequest request) {
		return (HttpEntityEnclosingRequest) request;
	}

	private String mapUriToLocalPath(HttpFileRequest fileRequest, String uri) {
		return fileRequest.mapper().mapUriRequestPath(uri);
	}

	private String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}

}
