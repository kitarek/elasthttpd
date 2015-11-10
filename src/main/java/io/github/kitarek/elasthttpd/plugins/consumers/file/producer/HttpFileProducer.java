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

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.commons.OptionalDispatcher;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.github.kitarek.elasthttpd.commons.Optional.empty;
import static io.github.kitarek.elasthttpd.commons.Optional.present;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.util.EncodingUtils.getAsciiBytes;

/**
 * Allows to setup HTTP response that will send over HTTP protocol an existing file from local filesystem using
 * the HTTP entity encoding
 *
 * Please note that this implementation currently does not handle charset detection of a file
 */
public class HttpFileProducer {

	public static final Logger LOGGER = LoggerFactory.getLogger(HttpFileProducer.class);
	public static final int STREAM_BUFFER_100KB_SIZE = 102400;

	/**
	 * Transform HttpResponse in such way it will contain the specified file as HTTP entity.
	 *
	 * @param localFile a valid not null file instance that must exist and be readable
	 * @param response a valid initially preconfigured HTTP response
	 */
	public void sendFileOverHttpResponse(final File localFile, final HttpResponse response) {
		notNull(localFile, "Local file must be not null");
		notNull(response, "HTTP response must be not null");
		isTrue(localFile.exists(), "The local file needs to exists: %s", localFile);
		isTrue(localFile.isFile(), "THe local file needs to be file in real and neither a directory nor device: %s",
				localFile);
		isTrue(!localFile.isDirectory(), "The local file must not be a directory: %s", localFile);
		isTrue(localFile.canRead(), "The local file must be at least readable: %s", localFile);


		final Optional<String> optionalContentType = detectMimeContentType(localFile);
		final Optional<InputStream> optionalInputStreamFromFile = transformFileIntoInputStream(localFile);
		final FileEntityMetadata fileEntityMetadata = new FileEntityMetadata(optionalContentType, localFile.length());
		sendInputStreamOverHttpResponse(optionalInputStreamFromFile, response, fileEntityMetadata);
	}

	private void sendInputStreamOverHttpResponse(Optional<InputStream> optionalInputStreamFromFile,
												 final HttpResponse response,
												 final FileEntityMetadata fileEntityMetadata) {
		optionalInputStreamFromFile.dispatch(new OptionalDispatcher<InputStream>() {
			public void notPresent() {
				setupInternalServerErrorResponse(response);
			}

			public void present(InputStream inputStream) {
				final FileEntity fileEntity = new FileEntity(inputStream, fileEntityMetadata);
				transformFileEntityIntoHttpEntity(fileEntity, response);
			}
		});
	}

	private Optional<InputStream> transformFileIntoInputStream(File localFile) {
		Optional<InputStream> optionalInputStreamFromFile;
		try {
			final InputStream inputStream = new BufferedInputStream(new FileInputStream(localFile),
					STREAM_BUFFER_100KB_SIZE);
			optionalInputStreamFromFile = present(inputStream);
		} catch (FileNotFoundException e) {
			optionalInputStreamFromFile = empty();
			LOGGER.error(format("Internal Server Error: Cannot open and read file: %s", localFile.getAbsolutePath()), e);
		}
		return optionalInputStreamFromFile;
	}

	private void transformFileEntityIntoHttpEntity(final FileEntity fileEntity, final HttpResponse response) {
		final FileEntityMetadata fileEntityMetadata = fileEntity.getFileEntityMetadata();
		fileEntityMetadata.getOptionalContentType().dispatch(new OptionalDispatcher<String>() {
			public void notPresent() {
				response.setEntity(new InputStreamEntity(fileEntity.getInputStreamFromFile(),
						fileEntityMetadata.getFileLength()));
			}

			public void present(String contentType) {
				response.setEntity(new InputStreamEntity(fileEntity.getInputStreamFromFile(),
						fileEntityMetadata.getFileLength(), ContentType.create(contentType)));
			}
		});
	}

	private Optional<String> detectMimeContentType(File localFile) {
		Optional<String> optionalContentType;
		try {
			String contentType = Files.probeContentType(Paths.get(localFile.getAbsolutePath()));
			optionalContentType = present(contentType);
		} catch (IOException e) {
			optionalContentType = empty();
			LOGGER.warn("There was an error checking file content type", e);
		}
		return optionalContentType;
	}

	private void setupInternalServerErrorResponse(HttpResponse response) {
		response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
		response.setReasonPhrase("INTERNAL SERVER ERROR");
		response.setEntity(new ByteArrayEntity(getAsciiBytes(
				"Internal Server Error while opening/reading server resource"),
				ContentType.create("text/plain", "US-ASCII")));
	}


	private static class FileEntity {
		private final InputStream inputStreamFromFile;
		private final FileEntityMetadata fileEntityMetadata;

		private FileEntity(InputStream inputStreamFromFile, final FileEntityMetadata fileEntityMetadata) {
			this.fileEntityMetadata = notNull(fileEntityMetadata);
			this.inputStreamFromFile = notNull(inputStreamFromFile);
		}

		public FileEntityMetadata getFileEntityMetadata() {
			return fileEntityMetadata;
		}

		public InputStream getInputStreamFromFile() {
			return inputStreamFromFile;
		}
	}

	private static class FileEntityMetadata {
		private final Optional<String> optionalContentType;
		private final long fileLength;

		private FileEntityMetadata(Optional<String> optionalContentType, long fileLength) {
			this.optionalContentType = notNull(optionalContentType);
			this.fileLength = notNull(fileLength);
		}

		public Optional<String> getOptionalContentType() {
			return optionalContentType;
		}

		public long getFileLength() {
			return fileLength;
		}
	}
}
