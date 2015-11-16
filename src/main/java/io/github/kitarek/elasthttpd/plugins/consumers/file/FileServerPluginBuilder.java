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

package io.github.kitarek.elasthttpd.plugins.consumers.file;

import io.github.kitarek.elasthttpd.commons.MimeTypeDetector;
import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder;
import io.github.kitarek.elasthttpd.plugins.consumers.ConsumerPluginBuilder;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumerFactory;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory.DirectorySubResourceRequestConsumer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory.ForbiddenDirectoryRequestConsumer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory.HttpDirectoryRequestConsumer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.request.HttpFileRequestFactory;
import io.github.kitarek.elasthttpd.plugins.consumers.file.selector.HttpFileRequestConsumerSelector;
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

public class FileServerPluginBuilder implements ConsumerPluginBuilder {

	private FileServerMode fileServerMode = FileServerMode.READ_ONLY;
	private File root;
	private final TemplatedHttpResponder templatedHttpResponder = new TemplatedHttpResponder();
	private MimeTypeDetector mimeTypeDetector = new MimeTypeDetector();
	private final HttpFileProducer httpFileProducer = new HttpFileProducer(mimeTypeDetector, templatedHttpResponder);
	private HttpDirectoryRequestConsumer httpDirectoryRequestConsumer = new ForbiddenDirectoryRequestConsumer(
			templatedHttpResponder);

	private FileServerPluginBuilder() {
	}

	/**
	 * FileServer plugin builder entry point.
	 *
	 * @return not null builder instance
	 */
	public static FileServerPluginBuilder fileServer() {
		return new FileServerPluginBuilder();
	}

	/**
	 * The helper method that is used to mark symbolically currently used directory. Use it if you would like to have
	 * relative root directory / of that server to server runtime directory.
	 *
	 * @return not null current working directory
	 */
	public static String currentDirectory() {
		Path currentRelativePath = Paths.get("");
		return currentRelativePath.toAbsolutePath().toString();
	}

	/**
	 * Define local server root directory that will be used to map '/' root resource URI. Any file outside
	 * of that directory will be not available to access in normal circumstances for that instance
	 *
	 * @param rootServerDirectory directory that will be mapped to '/' URL.
	 * @return this builder
	 */
	public FileServerPluginBuilder withRootServerDirectory(String rootServerDirectory) {
		notNull(rootServerDirectory, "Root server directory must be not null");
		root = new File(rootServerDirectory);
		isTrue(root.exists() && root.isDirectory() && root.canRead(),
				"The root directory needs to exists and be readable directory: '%s'", rootServerDirectory);
		return this;
	}

	/**
	 * Define the created filserver plugin instance capabilities.
	 *
	 * @param mode one of the available capabilities set (not null)
	 * @return this builder
	 */
	public FileServerPluginBuilder allowFileOperations(FileServerMode mode) {
		fileServerMode = notNull(mode, "FileServerMode must be not null");
		return this;
	}

	/**
	 * Respond with error when serving directory-related resources
	 * @return this builder
	 */
	public FileServerPluginBuilder forbidsAccessToDirectories() {
		httpDirectoryRequestConsumer = new ForbiddenDirectoryRequestConsumer(templatedHttpResponder);
		return this;
	}

	/**
	 * Use predefined sub-resource of that directory for resource resolution
	 *
	 * @param subresource the default resource under the served resource that will be used instead to produce
	 *                    HTTP response
	 * @return this builder
	 *
	 */
	public FileServerPluginBuilder serveSubresourceWhenDirectoryRequested(String subresource) {
		notNull(subresource, "Subresource must be not null");
		httpDirectoryRequestConsumer = new DirectorySubResourceRequestConsumer(httpFileProducer, templatedHttpResponder,
				subresource);
		return this;
	}

	public HttpRequestConsumer build() {
		notNull(root, "You need to invoke mandatory builder chain method: 'withRootServerDirectory' to define root server folder");
		HttpFileRequestFactory requestFactory = new HttpFileRequestFactory(root.getAbsolutePath());
		HttpFileRequestConsumerFactory consumerFactory = new HttpFileRequestConsumerFactory(templatedHttpResponder,
				httpDirectoryRequestConsumer, httpFileProducer);
		HttpFileRequestConsumerSelector selector = new HttpFileRequestConsumerSelector(fileServerMode, consumerFactory);
		return new HttpFileRequestConsumerDispatcher(requestFactory, selector);
	}

}
