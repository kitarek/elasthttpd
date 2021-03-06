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

package io.github.kitarek.elasthttpd.model;

/**
 * Type of the HttpMethod
 */
public enum HttpMethodType {
	/**
	 * General purpose method not requiring any special encoding or message body in request
	 */
	COMMON,

	/**
	 * The method most likely points to use encoded body entity to send additional data
	 */
	ENTITY_ENCODING,

	/**
	 * Specialized method most likely not requiring any additional in request
	 */
	SPECIAL
}
