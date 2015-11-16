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

package io.github.kitarek.elasthttpd.plugins.consumers;


import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer;

/**
 * The main entry-point interface for all external functionality modules which allows to integrate with main
 * ElastHttpD flexible builder.
 *
 * Each external plugin should contain at least one implementation of this class.
 *
 * Moreover the good practice is to create at least one custom static method that will return the builder implementation
 * class that will allow to create quickly instance of this builder.
 *
 * See {@link io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder} for reference.
 */
public interface ConsumerPluginBuilder {

	/**
	 * Build the resulting HttpRequestConsumer that will be used by HttpServer.
	 * This method is executed itself by the implementation of {@link io.github.kitarek.elasthttpd.ElastHttpDBuilder}
	 * in method {@link io.github.kitarek.elasthttpd.ElastHttpDBuilder#consumeRequestsWithPlugin(ConsumerPluginBuilder)}
	 * or by other plugins (their builders).
	 *
	 * @return valid not null HttpRequestConsumer instance that will be used by HttpServer.
	 */
	HttpRequestConsumer build();

}
