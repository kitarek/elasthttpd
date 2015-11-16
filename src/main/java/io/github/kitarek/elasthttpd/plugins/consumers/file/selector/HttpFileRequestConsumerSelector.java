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

import java.util.HashMap;
import java.util.Map;

import static io.github.kitarek.elasthttpd.commons.Optional.optional;
import static io.github.kitarek.elasthttpd.model.HttpMethod.*;
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode.READ_AND_WRITE;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Allows to select correct {@link HttpFileRequestConsumer} for a specified HTP method.
 */
public class HttpFileRequestConsumerSelector {

	private final Map<AnyHttpMethod, HttpFileRequestConsumer> methodToConsumerMap;

	public HttpFileRequestConsumerSelector(FileServerMode fileServerMode, HttpFileRequestConsumerFactory factory) {
		notNull(fileServerMode, "File server mode needs to be defined (not null)");
		notNull(factory, "The factory for HttpFileRequestConsumer needs to be a valid not null instance");
		methodToConsumerMap = new HashMap<AnyHttpMethod, HttpFileRequestConsumer>();
		createReadOnlyFileRequestConsumerSelector(factory);
		optionallyCreateReadWriteFileRequestConsumerSelector(fileServerMode, factory);
	}

	private void optionallyCreateReadWriteFileRequestConsumerSelector(FileServerMode fileServerMode,
																	  HttpFileRequestConsumerFactory factory) {
		if (fileServerMode == READ_AND_WRITE) {
			createReadWriteFileRequestConsumerSelector(factory);
		}
	}

	private void createReadWriteFileRequestConsumerSelector(HttpFileRequestConsumerFactory factory) {
		final HttpFileRequestConsumer consumerForDeleteOperation = factory.createConsumerForDeleteOperation();
		final HttpFileRequestConsumer consumerForWriteOperation = factory.createConsumerForWriteOperation();
		methodToConsumerMap.put(POST, consumerForWriteOperation);
		methodToConsumerMap.put(PUT, consumerForWriteOperation);
		methodToConsumerMap.put(DELETE, consumerForDeleteOperation);
	}

	private void createReadOnlyFileRequestConsumerSelector(HttpFileRequestConsumerFactory factory) {
		final HttpFileRequestConsumer consumerForReadOperation = factory.createConsumerForReadOperation();
		methodToConsumerMap.put(GET, consumerForReadOperation);
		methodToConsumerMap.put(HEAD, consumerForReadOperation);
	}

	/**
	 * Gets the hardcoded consumer type based on requested HTTP method (any available). Non standard methods are
	 * allowed too.
	 *
	 * @param anyHttpMethod
	 * @return optional reference to selected consumer (reference is present if method was registered with any consumer)
	 */
	public Optional<HttpFileRequestConsumer> selectConsumer(AnyHttpMethod anyHttpMethod) {
		return optional(methodToConsumerMap.get(anyHttpMethod));
	}

}
