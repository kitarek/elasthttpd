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

package io.github.kitarek.elasthttpd.server
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestConsumer
import io.github.kitarek.elasthttpd.server.consumers.HttpRequestPrimaryConsumer
import io.github.kitarek.elasthttpd.server.executors.ConsumerExecutor
import io.github.kitarek.elasthttpd.server.executors.HttpConnectionConsumerExecutor
import io.github.kitarek.elasthttpd.server.executors.HttpConnectionListenerExecutor
import io.github.kitarek.elasthttpd.server.executors.ListenerExecutor
import io.github.kitarek.elasthttpd.server.listeners.SynchronousStoppableHttpConnectionListener
import io.github.kitarek.elasthttpd.server.networking.BasicValidatedSocketConfiguration
import io.github.kitarek.elasthttpd.server.networking.HttpConfiguredServerSocket
import io.github.kitarek.elasthttpd.server.networking.SocketConfiguration
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionCompliantResponseProducer
import io.github.kitarek.elasthttpd.server.producers.HttpConnectionProducer
import org.apache.http.HttpEntity
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseFactory
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.DefaultHttpResponseFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HttpProcessor
import org.apache.http.protocol.HttpProcessorBuilder
import org.apache.http.protocol.ResponseConnControl
import org.apache.http.protocol.ResponseContent
import org.apache.http.protocol.ResponseDate
import org.apache.http.protocol.ResponseServer
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Timeout

import static org.apache.http.util.EncodingUtils.getAsciiBytes

class SimpleHttpServerLowLevelIntegrationSpec extends Specification {

	public static final Logger logger = LoggerFactory.getLogger(SimpleHttpServerLowLevelIntegrationSpec.class);
	public static final int NUMBER_OF_THREADS = 1
	public static final int HTTP_SERVER_PORT_NUMBER = 8888
	ListenerExecutor listenerExecutor
	SocketConfiguration socketConfiguration
	ConsumerExecutor consumerExecutor
	HttpResponseFactory httpResponseFactory
	HttpProcessor httpProcessor
	HttpConnectionProducer httpConnectionProducer



	def setup() {
		httpProcessor = createHttpProcessor();
		httpResponseFactory = DefaultHttpResponseFactory.INSTANCE
		socketConfiguration = new BasicValidatedSocketConfiguration(Inet4Address.getByName("localhost"),
				HTTP_SERVER_PORT_NUMBER, 100);
		httpConnectionProducer = new HttpConnectionCompliantResponseProducer(httpProcessor)
		consumerExecutor = new HttpConnectionConsumerExecutor(NUMBER_OF_THREADS)
		listenerExecutor = new HttpConnectionListenerExecutor()
	}

	private HttpProcessor createHttpProcessor() {
		HttpProcessor httpProcessorCopy
		final HttpProcessorBuilder b = HttpProcessorBuilder.create();
		b.addAll(
				new ResponseDate(),
				new ResponseServer("ElastHttpD/1.1"),
				new ResponseContent(),
				new ResponseConnControl());
		httpProcessorCopy = b.build();
		httpProcessorCopy
	}

	@Timeout(20)
	def 'Run server with default HELLO response and request GET method on / request'() {
		given: "Use HttpClient for issuing request to server"
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		and: "Let's instruct server to always respond with HELLO"
			final HttpServer server = configureSimpleHttpServer(new HttpRequestConsumer() {
				@Override
				void consumeRequest(HttpRequest request, HttpResponse response) {
					response.setEntity(new ByteArrayEntity(getAsciiBytes("HELLO")))
				}
			})
		and:
			def actualResponse

		when: "Start server in separate thread"
			def thread = Thread.start {
				logger.info("Starting HTTP server");
				server.start();
				logger.info("Waiting server to be stopped");
				server.waitUntilStopped()
				logger.info("Server has been stopped");
			}
		and: "Stop the server and client after 2 seconds"
			Thread.start {
				sleep(2000)
				logger.info("Stopping HTTP client and HTTP server under test");
				httpclient.close();
				server.stop();
			}
		and:
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

	private configureSimpleHttpServer(HttpRequestConsumer httpRequestConsumer) {
		def connectionConsumer = new HttpRequestPrimaryConsumer(httpResponseFactory, httpProcessor,
				httpConnectionProducer, httpRequestConsumer)
		def connectionListener = new SynchronousStoppableHttpConnectionListener(consumerExecutor, connectionConsumer)
		def listeningSocket = HttpConfiguredServerSocket.newHttpConfiguredServerSocket(socketConfiguration)
		new SimpleHttpServer(listenerExecutor, connectionListener, listeningSocket)
	}


}
