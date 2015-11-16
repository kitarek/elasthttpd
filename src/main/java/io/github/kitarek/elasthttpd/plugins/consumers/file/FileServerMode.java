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

package io.github.kitarek.elasthttpd.plugins.consumers.file;

/**
 * Describes the basic capabilities of file server plugin instance
 */
public enum FileServerMode {
	/**
	 * Allows only to read/download files but the filesystem state cannot be changed.
	 */
	READ_ONLY,
	/**
	 * Allows to modify file and/or directories on HttpServer side as well as reading any resources exposed via
	 * file server.
	 */
	READ_AND_WRITE
}
