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

import io.github.kitarek.elasthttpd.commons.Optional;

import java.util.HashMap;
import java.util.Map;

import static io.github.kitarek.elasthttpd.model.HttpMethodScope.*;
import static io.github.kitarek.elasthttpd.model.HttpMethodType.*;
import static io.github.kitarek.elasthttpd.commons.Optional.optional;

public enum HttpMethod {
	GET("GET", COMMON, WITHOUT_SIDE_EFFECT),
	POST("POST", ENTITY_ENCODING, CAUSES_SIDE_EFFECT),
	PUT("PUT", ENTITY_ENCODING, CAUSES_SIDE_EFFECT),
	HEAD("HEAD", SPECIAL, WITHOUT_SIDE_EFFECT),
	TRACE("TRACE", SPECIAL, UNKNOWN),
	OPTIONS("OPTIONS", SPECIAL, WITHOUT_SIDE_EFFECT),
	DELETE("DELETE", SPECIAL, CAUSES_SIDE_EFFECT),
	CONNECT("CONNECT", SPECIAL, UNKNOWN);

	private static Map<String, HttpMethod> methodMap = new HashMap<String, HttpMethod>(HttpMethod.values().length);

	static {
		for (HttpMethod method : values())
			methodMap.put(method.id, method);
	}

	public static Optional<HttpMethod> fromString(String methodIdentifier) {
		return optional(methodMap.get(methodIdentifier));
	}

	HttpMethod(String identifier, HttpMethodType type, HttpMethodScope scope) {
		this.id = identifier;
		this.type = type;
		this.scope = scope;
	};

	public final String id;
	public final HttpMethodScope scope;
	public final HttpMethodType type;
}
