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

package io.github.kitarek.elasthttpd.commons;


import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.*;
import static org.apache.http.util.EncodingUtils.getAsciiBytes;

/**
 * Allows to use response templates (using text/plain encoding) for typical HTTP statuses.
 */
public class TemplatedHttpResponder {

	/**
	 * Setup INTERNAL SERVER ERROR HttpResponse with a given text/plain message send as response body.
	 *
	 * @param response not-null
	 * @param message not-null
	 */
	public void respondWithInternalServerError(HttpResponse response, String message) {
		validateResponseAndMessage(response, message);
		response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
		response.setReasonPhrase("INTERNAL SERVER ERROR");
		setupAsciiUsStringAsResponseEntity(response, message);
	}

	/**
	 * Setup NOT FOUND response for a given HttpResponse. The specified message will be used as
	 * response body content (text/plain).
	 *
	 * @param response not null
	 * @param message not null
	 */
	public void respondWithResourceNotFound(HttpResponse response, String message) {
		validateResponseAndMessage(response, message);
		response.setStatusCode(SC_NOT_FOUND);
		response.setReasonPhrase("NOT FOUND");
		setupAsciiUsStringAsResponseEntity(response, message);
	}

	/**
	 * Setup FORBIDDEN response for a given HttpResponse. The specified message will be used as
	 * response body content (text/plain).
	 *
	 * @param response not null
	 * @param message not null
	 */
	public void respondWithResourceForbidden(HttpResponse response, String message) {
		validateResponseAndMessage(response, message);
		response.setStatusCode(SC_FORBIDDEN);
		response.setReasonPhrase("FORBIDDEN");
		setupAsciiUsStringAsResponseEntity(response, message);
	}

	/**
	 * Setup 204 (no content) DELETED response for a given HttpResponse.
	 *
	 * @param response not null
	 */
	public void respondWithNoContentAndReasonDeleted(HttpResponse response) {
		response.setStatusCode(SC_NO_CONTENT);
		response.setReasonPhrase("DELETED");
	}

	private void validateResponseAndMessage(HttpResponse response, String message) {
		notNull(response, "HttpResponse to use with template must be not null");
		notNull(message, "Message to use in template must be not null");
	}

	private void setupAsciiUsStringAsResponseEntity(HttpResponse response, String message) {
		response.setEntity(new ByteArrayEntity(getAsciiBytes(message),
				ContentType.create("text/plain", "US-ASCII")));
	}

	/**
	 * Setup CREATED HTTP status code for a given HttpResponse. Response body is not allowed hence any message cannot
	 * be passed here.

	 * @param response not null
	 */
	public void respondThatResourceIsCreated(HttpResponse response) {
		response.setStatusCode(SC_CREATED);
		response.setReasonPhrase("CREATED");
	}
}
