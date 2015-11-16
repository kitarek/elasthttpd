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
 * Represents RFC2616-related HTTP method or completely additional one that can be used as extension for HTTP procotol.
 *
 * @link https://tools.ietf.org/html/rfc2616#section-5.1.1
 */
public interface AnyHttpMethod {

	/**
	 * Returns the general scope of the method. The scope described what can be affected if one request is sent
	 * with the following method. In example if scope is one request then only one current response of that request
	 * is affected.
	 *
	 * However if the scope is bigger than in general that method causes side effects and indirectly changes states
	 * and responses of other methods.
	 *
	 * @return the one from enumeration of method scope (always not null)
	 */
	HttpMethodScope getScope();

	/**
	 * Describes the type of HTTP method. Some HTTP methods and requests uses those methods can supply additional
	 * information or could have special meaning. Refer to: {@link HttpMethodType} for more details.
	 *
	 * @return the one from enumeration of method type (always not null)
	 */
	HttpMethodType getType();

	/**
	 * Get textual string representing reuqets method. In example for GET request it returns 'GET', for PUT
	 * request it returns 'PUT' etc.
	 *
	 * @return always not null string
	 */
	String getId();

}
