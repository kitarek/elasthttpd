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
 * Each HTTP request method by its nature can or cannot modify the server status. That property is called scope.
 * For each method there is a contract that on can cause side effects (modify server state) or cannot.
 *
 * For some methods it can depend on different things and basically we don't know what the method scope is
 */
public enum HttpMethodScope {
	/**
	 * Most likely method affects server state, however surely not for each request
	 */
	CAUSES_SIDE_EFFECT,

	/**
	 * This method cannot modify server state for any request whatever its parameters are given
	 */
	WITHOUT_SIDE_EFFECT,

	/**
	 * In general scope is uknown so it's better in general that it can cause side effects (which may not happen at all)
	 */
	UNKNOWN
}
