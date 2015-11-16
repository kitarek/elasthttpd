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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.Validate.*;

/**
 * Maps server URIs to local filesystem using one directory as base root path for mapping all resources.
 */
public class UriToFileMapper {

	public static final Logger LOGGER = LoggerFactory.getLogger(UriToFileMapper.class);
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
		String localRequestPath;
		try {
			localRequestPath = URLDecoder.decode(uriRequestPath, Charset.defaultCharset().displayName());
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Cannot decode URI. Using original URI path for file", e);
			localRequestPath = uriRequestPath;
		}
		final Path p = Paths.get(localRequestPath);
		return p.normalize().toAbsolutePath().toString();
	}

}
