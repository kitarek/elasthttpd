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
import spock.lang.Specification

class ElastHttpDSpec extends Specification {

	def 'Allows to build HTTP server with globally defined server info'() {
		given:
			def httpdBuilder = ElastHttpD.startBuilding()

		when:
			def httpd = httpdBuilder.serverInfo("my server info").createAndReturn();

		then:
			httpd != null
			httpd instanceof HttpServer
	}

	def 'By default HTTP server runs in current thread at least one second'() {
		given:
			def serverRunnable = new Runnable() {
				@Override
				void run() {
					ElastHttpD.startBuilding().run();
				}
			}
		and:
			def currentThread = new Thread(serverRunnable);

		when:
			currentThread.start();
		and:
			sleep(1000)

		then:
			currentThread.alive == true

		cleanup:
			currentThread.interrupt();
	}
}
