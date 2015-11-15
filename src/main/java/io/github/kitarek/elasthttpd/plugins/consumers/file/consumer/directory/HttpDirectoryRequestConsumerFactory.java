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

package io.github.kitarek.elasthttpd.plugins.consumers.file.consumer.directory;

import io.github.kitarek.elasthttpd.commons.TemplatedHttpResponder;
import io.github.kitarek.elasthttpd.plugins.consumers.file.producer.HttpFileProducer;

import static org.apache.commons.lang3.Validate.notNull;

public class HttpDirectoryRequestConsumerFactory {
	private final TemplatedHttpResponder templatedHttpResponder;
	private final HttpFileProducer httpFileProducer;

	public HttpDirectoryRequestConsumerFactory(TemplatedHttpResponder templatedHttpResponder, HttpFileProducer httpFileProducer) {
		this.templatedHttpResponder = notNull(templatedHttpResponder);
		this.httpFileProducer = notNull(httpFileProducer);
	}

	public HttpDirectoryRequestConsumer createConsumerThatForbidsAccessToDirectories() {
		return new ForbiddenDirectoryRequestConsumer(templatedHttpResponder);
	}

	public HttpDirectoryRequestConsumer createConsumerThatAllowsToAccessSubResourceForDirectories(String subresource) {
		return new DirectorySubResourceRequestConsumer(httpFileProducer, templatedHttpResponder,
				notNull(subresource, "Subresource for directories cannot be null"));
	}
}
