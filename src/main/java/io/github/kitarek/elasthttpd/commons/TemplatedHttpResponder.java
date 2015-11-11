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
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.util.EncodingUtils.getAsciiBytes;

public class TemplatedHttpResponder {

	public void respondWithInternalServerError(HttpResponse response, String message) {
		notNull(response, "HttpResponse to use with template must be not null");
		notNull(message, "Message to use in tempalte must be not null");
		response.setStatusCode(SC_INTERNAL_SERVER_ERROR);
		response.setReasonPhrase("INTERNAL SERVER ERROR");
		response.setEntity(new ByteArrayEntity(getAsciiBytes(message),
				ContentType.create("text/plain", "US-ASCII")));
	}
}