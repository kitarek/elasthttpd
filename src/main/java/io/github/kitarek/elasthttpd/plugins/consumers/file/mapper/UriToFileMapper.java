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

package io.github.kitarek.elasthttpd.plugins.consumers.file.mapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.Validate.*;


public class UriToFileMapper {

	public static final String ROOT_URI_REQUEST_PATH = "/";
	public static final String RESOURCE_PATH_SEPARATOR = "/";
	private final String pathToMappedRootDirectory;

	public UriToFileMapper(String pathToMappedRootDirectory) {
		this.pathToMappedRootDirectory =
				defaultIfEmpty(
					removeEnd(
						notEmpty(pathToMappedRootDirectory, "Path of root directory for mapping cannot be empty"),
					RESOURCE_PATH_SEPARATOR),
				ROOT_URI_REQUEST_PATH);
		isTrue(isValidDirectoryForMapping(this.pathToMappedRootDirectory),
				"Path needs to point to needs to be an absolute existing and readable directory that will be mapped.");
	}

	public static boolean isValidDirectoryForMapping(String absolutePathToDirectory) {
		notNull(absolutePathToDirectory, "An absolute path to directory cannot be null");
		return isValidExistingAbsoluteDirectory(new File(absolutePathToDirectory));
	}

	private static boolean isValidExistingAbsoluteDirectory(File f) {
		return f.canRead() && f.exists() && f.isAbsolute() && f.isDirectory();
	}

	public static boolean isCorrectUriRequestPath(String uriRequestPath) {
		return !isBlank(uriRequestPath) && uriRequestPath.startsWith(ROOT_URI_REQUEST_PATH);
	}

	public String mapUriRequestPath(String uriRequestPath) {
		isTrue(isCorrectUriRequestPath(uriRequestPath), "URI request path is not correct");
		return removeEnd(pathToMappedRootDirectory + normalizeUrlRequestPath(uriRequestPath), RESOURCE_PATH_SEPARATOR);
	}

	private String normalizeUrlRequestPath(String uriRequestPath) {
		Path p = Paths.get(uriRequestPath);
		return p.normalize().toAbsolutePath().toString();
	}

}
