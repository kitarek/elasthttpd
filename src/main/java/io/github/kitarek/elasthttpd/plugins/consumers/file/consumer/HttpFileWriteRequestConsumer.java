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
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Consumer that is able to write one file send in HTTP request body and declared as octet-stream.
 * The current implementation tries to write a file as soon as possible without any checksum verification into
 * destination place.
 *
 * Currently no respnse body is sent to client with the URL of created resource.
 */
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

	protected void doWithRequestedFile(HttpFileRequest fileRequest, File file) {
		if (file.isDirectory())
			respondThatResourceIsForbidden(fileRequest);
		else
			tryToWriteFileAndRespondToRequest(fileRequest, file);
	}

	protected void respondThatResourceIsForbidden(HttpFileRequest fileRequest) {
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

	protected void handleFileNotFoundException(HttpFileRequest fileRequest, File file, FileNotFoundException e) {
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

	private void decodeRequestEntityAndWriteToFile(HttpFileRequest fileRequest, File file,
												   HttpEntityEnclosingRequest httpEntityEnclosingRequest) {
		final HttpEntity entity = httpEntityEnclosingRequest.getEntity();
		try {
			decodeRequestEntityAndWriteToFileUnchecked(fileRequest, file, entity);
		} catch (FileNotFoundException e) {
			handleFileNotFoundException(fileRequest, file, e);
		}
	}

	private void decodeRequestEntityAndWriteToFileUnchecked(HttpFileRequest fileRequest, File file, HttpEntity entity) throws FileNotFoundException {
		OutputStream outputStream = new FileOutputStream(file);
		writeAndFlush(file, entity, outputStream);
		respondThatResourceIsCreated(fileRequest);
	}

	private void respondThatResourceIsCreated(HttpFileRequest fileRequest) {
		templatedHttpResponder.respondThatResourceIsCreated(fileRequest.response());
		// TODO ... create an URL
	}

	void writeAndFlush(File file, HttpEntity entity, OutputStream outputStream) {
		try {
			writeAndFlushUnchecked(entity, outputStream);
		} catch (IOException e) {
			LOGGER.error(format("There was an error with writing or flushing stream to file: %s",
					file.getAbsolutePath()), e);
			flushTheFile(file, outputStream, e);
		} finally {
			closeTheStream(outputStream, file);
		}
	}

	private void writeAndFlushUnchecked(HttpEntity entity, OutputStream outputStream) throws IOException {
		entity.writeTo(outputStream);
		outputStream.flush();
	}

	void flushTheFile(File file, OutputStream outputStream, IOException e) {
		try {
			outputStream.flush();
		} catch (IOException e1) {
			LOGGER.error(format("There was an error with flushing stream to file: %s",
					file.getAbsolutePath()), e);
		}
	}

	protected boolean isRequestImplementingEntity(HttpRequest request) {
		return (request instanceof HttpEntityEnclosingRequest);
	}

	protected HttpEntityEnclosingRequest upgradeHttpRequestToSupportEntities(HttpRequest request) {
		return (HttpEntityEnclosingRequest) request;
	}

	private String mapUriToLocalPath(HttpFileRequest fileRequest, String uri) {
		return fileRequest.mapper().mapUriRequestPath(uri);
	}

	protected String getRequestedUri(HttpFileRequest fileRequest) {
		return fileRequest.request().getRequestLine().getUri();
	}

}
