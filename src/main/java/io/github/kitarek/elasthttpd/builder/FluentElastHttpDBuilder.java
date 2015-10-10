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

package io.github.kitarek.elasthttpd.builder;

import io.github.kitarek.elasthttpd.ElastHttpDBuilder;
import io.github.kitarek.elasthttpd.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluentElastHttpDBuilder implements ElastHttpDBuilder {

	Logger logger = LoggerFactory.getLogger(FluentElastHttpDBuilder.class);

	public ElastHttpDBuilder serverInfo(String serverInfoLine) {
		return this;
	}

	public HttpServer createAndReturn() {
		return new HttpServer() {};
	}

	public void run() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			logger.warn("Server has been interrupted");
		}
	}
}
