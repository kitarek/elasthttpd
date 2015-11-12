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

import io.github.kitarek.elasthttpd.plugins.consumers.ConsumerPluginBuilder;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumerFactory;
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

	private FileServerPluginBuilder() {
	}

	public static FileServerPluginBuilder fileServer() {
		return new FileServerPluginBuilder();
	}

	public static String currentDirectory() {
		Path currentRelativePath = Paths.get("");
		return currentRelativePath.toAbsolutePath().toString();
	}

	public FileServerPluginBuilder withRootServerDirectory(String rootServerDirectory) {
		notNull(rootServerDirectory, "Root server directory must be not null");
		root = new File(rootServerDirectory);
		isTrue(root.exists() && root.isDirectory() && root.canRead(),
				"The root directory needs to exists and be readable directory: '%s'", rootServerDirectory);
		return this;
	}

	public FileServerPluginBuilder allowFileOperations(FileServerMode mode) {
		fileServerMode = notNull(mode, "FileServerMode must be not null");
		return this;
	}

	public HttpRequestConsumer build() {
		notNull(root, "You need to invoke mandatory builder chain method: 'withRootServerDirectory' to define root server folder");
		HttpFileRequestFactory requestFactory = new HttpFileRequestFactory(root.getAbsolutePath());
		HttpFileRequestConsumerFactory consumerFactory = new HttpFileRequestConsumerFactory();
		HttpFileRequestConsumerSelector selector = new HttpFileRequestConsumerSelector(fileServerMode, consumerFactory);
		return new HttpFileRequestConsumerDispatcher(requestFactory, selector);
	}

}
