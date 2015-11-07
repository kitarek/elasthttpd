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

package io.github.kitarek.elasthttpd.plugins.consumers.file.selector;

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.model.AnyHttpMethod;
import io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumer;
import io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.HttpFileRequestConsumerFactory;

import static io.github.kitarek.elasthttpd.commons.Optional.empty;

public class HttpFileRequestConsumerSelector {

	public HttpFileRequestConsumerSelector(FileServerMode fileServerMode, HttpFileRequestConsumerFactory factory) {
		createReadOnlyFileRequestConsumerSelector(factory);
		if (fileServerMode == FileServerMode.READ_AND_WRITE) {
			createReadWriteFileRequestConsumerSelector(factory);
		}
	}

	private void createReadWriteFileRequestConsumerSelector(HttpFileRequestConsumerFactory factory) {
	}

	private void createReadOnlyFileRequestConsumerSelector(HttpFileRequestConsumerFactory factory) {
	}

	public Optional<HttpFileRequestConsumer> selectConsumer(AnyHttpMethod anyHttpMethod) {
		return empty();
	}

}
