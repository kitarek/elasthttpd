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

package io.github.kitarek.elasthttpd

import io.github.kitarek.elasthttpd.server.HttpServer
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer
import org.apache.http.HttpEntity
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Timeout

import static io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder.newConfiguration
import static org.apache.http.util.EncodingUtils.getAsciiBytes

class ElastHttpDIntegrationSpec extends Specification {
	public static final Logger logger = LoggerFactory.getLogger(ElastHttpDIntegrationSpec.class);
	public static final int HTTP_SERVER_PORT_NUMBER = 8889

	@Timeout(20)
	def 'Run server with default HELLO response and request GET method on / request'() {
		given: "Use HttpClient for issuing request to server"
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		and: "Let's instruct server to always respond with HELLO"
			def customeRequestConsumer = new HttpRequestConsumer() {
				@Override
				void consumeRequest(HttpRequest request, HttpResponse response) {
					response.setEntity(new ByteArrayEntity(getAsciiBytes("HELLO")))
				}
			}
		and:
			def actualResponse

		when: "Configure server"
			def HttpServer server = ElastHttpD
					.startBuilding()
					.customRequestConsumer(customeRequestConsumer)
					.networkConfiguration(
						newConfiguration().setListeningPort(HTTP_SERVER_PORT_NUMBER))
					.createAndReturn()
		and: "Start server in separate thread"
			logger.info("Starting HTTP server");
			server.start();
		and: "Stop the server and client after 2 seconds"
			Thread.start {
				sleep(2000)
				logger.info("Stopping HTTP client and HTTP server under test");
				httpclient.close();
				server.stop();
			}
		and: "Let's instruct HTTP client to save HTTP server response into string"
			try {
				HttpGet httpget = new HttpGet("http://localhost:" + HTTP_SERVER_PORT_NUMBER +  "/");
				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
					@Override
					public String handleResponse(
							final HttpResponse response) throws ClientProtocolException, IOException {
						logger.info("Handling response");
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							HttpEntity entity = response.getEntity();
							return entity != null ? EntityUtils.toString(entity) : null;
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					}
				};
				logger.info("Executing request to server...")
				actualResponse = httpclient.execute(httpget, responseHandler);
			} finally {
				httpclient.close();
			}
		and:
			logger.info("Waiting for server to be stopped");
			server.waitUntilStopped()
			logger.info("Server has been stopped");

		then:
			actualResponse == "HELLO"
	}
}
