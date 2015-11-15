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

package io.github.kitarek.elasthttpd.plugins.consumers.file
import io.github.kitarek.elasthttpd.ElastHttpD
import io.github.kitarek.elasthttpd.server.HttpServer
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.FileEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Timeout

import java.nio.file.Files
import java.nio.file.Paths

import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerMode.READ_AND_WRITE
import static io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder.fileServer
import static io.github.kitarek.elasthttpd.server.networking.NetworkConfigurationBuilder.newConfiguration
import static java.nio.charset.StandardCharsets.UTF_8
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex

class ElastHttpDFileServerPluginIntegrationSpec extends Specification {
	public static final Logger LOGGER = LoggerFactory.getLogger(ElastHttpDFileServerPluginIntegrationSpec.class);
	public static final int HTTP_SERVER_PORT_NUMBER = 8900;

	@Timeout(20)
	def 'Run server with file server plugin configured for test resources dir and invoke GET request /test-file.txt'() {
		given: "Use HttpClient for issuing request to server"
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		and:
			def serverRootDirForTest = "./src/test/resources"
			def requestedFile = "/test-file.txt"
		and:
			def expectedResponse = new String(Files.readAllBytes(Paths.get(serverRootDirForTest + requestedFile)), UTF_8);
			def actualResponse

		when: "Configure ElastHttpD server"
			def HttpServer server = ElastHttpD
				.startBuilding()
				.consumeRequestsWithPlugin(
					fileServer().withRootServerDirectory(serverRootDirForTest)
				)
				.networkConfiguration(
				newConfiguration().setListeningPort(HTTP_SERVER_PORT_NUMBER))
				.createAndReturn()
		and: "Start server in separate thread"
			LOGGER.info("Starting HTTP server");
			server.start();
		and: "Stop the server and client after 2 seconds"
			Thread.start {
				sleep(2000)
				LOGGER.info("Stopping HTTP client and HTTP server under test");
				httpclient.close();
				server.stop();
			}
		and: "Let's instruct HTTP client to save HTTP server response into string"
			try {
				HttpGet httpget = new HttpGet("http://localhost:" + HTTP_SERVER_PORT_NUMBER + requestedFile);
				ResponseHandler<String> responseHandler = getStringResponderHandler()
				LOGGER.info("Executing request to server...")
				actualResponse = httpclient.execute(httpget, responseHandler);
			} finally {
				httpclient.close();
			}
		and:
			LOGGER.info("Waiting for server to be stopped");
			server.waitUntilStopped()
			LOGGER.info("Server has been stopped");

		then:
			actualResponse == expectedResponse
	}

	@Timeout(20)
	def 'Run server with file server plugin configured for test resources dir and invoke POST request with text file entity to /upload-file.txt'() {
		given: "Use HttpClient for issuing request to server"
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		and:
			def serverRootDirForTest = "./src/test/resources"
			def existingFile = "/test-file.txt"
			def requestedFile = "/upload-file.txt"
		and:
			def expectedResponse = ""
			def actualResponse
			def expectedFileContentToBeTransferred = new String(Files.readAllBytes(Paths.get(serverRootDirForTest + existingFile)), UTF_8);

		when: "Configure ElastHttpD server"
			def HttpServer server = ElastHttpD
				.startBuilding()
				.consumeRequestsWithPlugin(
					fileServer().withRootServerDirectory(serverRootDirForTest).allowFileOperations(READ_AND_WRITE)
				)
				.networkConfiguration(
				newConfiguration().setListeningPort(HTTP_SERVER_PORT_NUMBER))
				.createAndReturn()
		and: "Start server in separate thread"
			LOGGER.info("Starting HTTP server");
			server.start();
		and: "Stop the server and client after 2 seconds"
			Thread.start {
				sleep(2000)
				LOGGER.info("Stopping HTTP client and HTTP server under test");
				httpclient.close();
				server.stop();
			}
		and: "Let's instruct HTTP client to save HTTP server response into string"
			try {
				HttpPost httpPost = new HttpPost("http://localhost:" + HTTP_SERVER_PORT_NUMBER + requestedFile);
				httpPost.setEntity(new FileEntity(new File(serverRootDirForTest + existingFile),
						ContentType.APPLICATION_OCTET_STREAM))
				ResponseHandler<String> responseHandler = getStringResponderHandler();
				LOGGER.info("Executing request to server...")
				actualResponse = httpclient.execute(httpPost, responseHandler);
			} finally {
				httpclient.close();
			}
		and:
			LOGGER.info("Waiting for server to be stopped");
			server.waitUntilStopped()
			LOGGER.info("Server has been stopped");
		and:
			def actualFileContentTransferred = new String(Files.readAllBytes(Paths.get(serverRootDirForTest + requestedFile)), UTF_8)


		then:
			actualFileContentTransferred == expectedFileContentToBeTransferred;
//			actualResponse == expectedResponse

		cleanup:
			new File(serverRootDirForTest + requestedFile).delete()
	}

	@Timeout(20)
	def 'Run server with file server plugin configured for test resources dir and invoke POST request with bigger entity to /upload-file.pdf'() {
		given: "Use HttpClient for issuing request to server"
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		and:
			def serverRootDirForTest = "./src/test/resources"
			def existingFile = "/test-file.pdf"
			def requestedFile = "/upload-file.pdf"
		and:
			def expectedResponse = ""
			def actualResponse
			def expectedFileDigestToBeTransferred = sha256Hex(new FileInputStream(serverRootDirForTest + existingFile))

		when: "Configure ElastHttpD server"
			def HttpServer server = ElastHttpD
					.startBuilding()
					.consumeRequestsWithPlugin(
					fileServer().withRootServerDirectory(serverRootDirForTest).allowFileOperations(READ_AND_WRITE)
				)
				.networkConfiguration(
				newConfiguration().setListeningPort(HTTP_SERVER_PORT_NUMBER))
				.createAndReturn()
		and: "Start server in separate thread"
			LOGGER.info("Starting HTTP server");
			server.start();
		and: "Stop the server and client after 2 seconds"
			Thread.start {
				sleep(2000)
				LOGGER.info("Stopping HTTP client and HTTP server under test");
				httpclient.close();
				server.stop();
			}
		and: "Let's instruct HTTP client to save HTTP server response into string"
			try {
				HttpPost httpPost = new HttpPost("http://localhost:" + HTTP_SERVER_PORT_NUMBER + requestedFile);
				httpPost.setEntity(new FileEntity(new File(serverRootDirForTest + existingFile),
						ContentType.APPLICATION_OCTET_STREAM))
				ResponseHandler<String> responseHandler = getStringResponderHandler();
				LOGGER.info("Executing request to server...")
				actualResponse = httpclient.execute(httpPost, responseHandler);
			} finally {
				httpclient.close();
			}
		and:
			LOGGER.info("Waiting for server to be stopped");
			server.waitUntilStopped()
			LOGGER.info("Server has been stopped");
		and:
			def actualFileDigestToBeTransferred = sha256Hex(new FileInputStream(serverRootDirForTest + requestedFile))


		then:
			actualFileDigestToBeTransferred == expectedFileDigestToBeTransferred
//			actualResponse == expectedResponse

		cleanup:
			new File(serverRootDirForTest + requestedFile).delete()
	}


	private getStringResponderHandler() {
		new ResponseHandler<String>() {
			@Override
			public String handleResponse(
					final HttpResponse response) throws ClientProtocolException, IOException {
				LOGGER.info("Handling response");
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}
		}
	}


}
