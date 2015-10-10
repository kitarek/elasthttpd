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

package io.github.kitarek.elasthttpd.story;

import io.github.kitarek.elasthttpd.commons.Optional;
import io.github.kitarek.elasthttpd.commons.OptionalDispatcher;
import io.github.kitarek.elasthttpd.model.HttpMethod;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;

import static io.github.kitarek.elasthttpd.model.HttpMethod.GET;
import static io.github.kitarek.elasthttpd.model.HttpMethod.fromString;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED;
import static org.apache.http.HttpStatus.SC_OK;

public class OneResponseStory implements HttpRequestHandler {

	public static final ContentType DEFAULT_CONTENT_TYPE = ContentType.create("text/html", "UTF-8");

	public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
		Optional<HttpMethod> optionalHttpMethod = fromString(request.getRequestLine().getMethod());
		optionalHttpMethod.dispatch(new OptionalDispatcher<HttpMethod>() {
			public void notPresent() {	respondWithMthodNotAllowed(response); }
			public void present(HttpMethod method) { handleGetMethodRefuseTheRest(method, response); }
		});
	}

	private void handleGetMethodRefuseTheRest(HttpMethod method, HttpResponse response) {
		if (method == GET)
			respondWithStaticAnswer(response);
		else
			respondWithMethodNotImplemented(response);
	}



	private void respondWithMethodNotImplemented(HttpResponse response) {
		respondWithStatusAndText(response, SC_NOT_IMPLEMENTED, notImplementedMessage());
	}

	private String notImplementedMessage() {
		return htmlWithTitleAndMessage("Not Implemented", "This method is not implemented");
	}



	private void respondWithStaticAnswer(HttpResponse response) {
		respondWithStatusAndText(response, SC_OK, helloWorldMessage());
	}

	private String helloWorldMessage() {
		return htmlWithTitleAndMessage("Hello World", "This is default static response for GET request");
	}



	private void respondWithMthodNotAllowed(HttpResponse response) {
		respondWithStatusAndText(response, SC_METHOD_NOT_ALLOWED, methodNotAllowedMessage());
	}

	private String methodNotAllowedMessage() {
		return htmlWithTitleAndMessage("Method Not Allowed", "This method is not allowed - it is unknown for HTTP 1");
	}



	private void respondWithStatusAndText(HttpResponse response, int scNotImplemented, String string) {
		response.setStatusCode(scNotImplemented);
		response.setEntity(new StringEntity(string, DEFAULT_CONTENT_TYPE));
	}

	private String htmlWithTitleAndMessage(String title, String message) {
		return format("<html><body><h1>%s</h1><p>%s</p></body></html>", title, message);
	}

}
