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

package io.github.kitarek.elasthttpd.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.github.kitarek.elasthttpd.commons.Optional.empty;
import static io.github.kitarek.elasthttpd.commons.Optional.optional;

/**
 * Encapsulates MIME type detection for an existing file.
 */
public class MimeTypeDetector {

	public static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeDetector.class);

	/**
	 * Detect MIME type of the existing local file/directoru (filesystem object). If file does not exist the MIME type
	 * string won't be present. If it was unable to detect MIME-type of file or directory the string won't be present.
	 *
	 * @param localFile not-null reference to a file
	 * @return optional string object for which you need to explicitly check if it is present.
	 */
	public Optional<String> detectMimeContentType(File localFile) {
		Optional<String> optionalContentType;
		try {
			String contentType = Files.probeContentType(Paths.get(localFile.getAbsolutePath()));
			optionalContentType = optional(contentType);
		} catch (IOException e) {
			optionalContentType = empty();
			LOGGER.warn("There was an error checking file content type", e);
		}
		return optionalContentType;
	}

}
