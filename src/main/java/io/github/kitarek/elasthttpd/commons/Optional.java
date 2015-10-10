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

import org.apache.commons.lang3.Validate;

/**
 * Optional type inspired on Java 8 Optional that address null-reference problem. In case of direct access (it is
 * still possible) runtime exception will be thrown to fail fast in case value is not assigned (null reference).
 *
 * @param <T> any type for which we would like to handle null reference that can occur
 */
public class Optional<T> {

	private T reference;

	private Optional(T value) {
		this.reference = value;
	}

	private Optional() {
	}

	public static <T> Optional<T> empty() {
		return new Optional<T>();
	}

	public static <T> Optional<T> present(T value) {
		Validate.notNull(value, "The value for 'present(value)' method is mandatory and must be present -- not null!");
		return optional(value);
	}

	public static <T> Optional<T> optional(T value) {
		return new Optional<T>(value);
	}

	public boolean isPresent() {
		return (reference != null);
	}

	public boolean isNotPresent() {
		return !isPresent();
	}

	public T get() {
		Validate.notNull(reference, "Cannot access optional value that is not present. Check first for presence");
		return reference;
	}

	public void map(OptionalMapper<T> mapper) {
		if (isPresent()) {
			mapper.present(reference);
		}
	}

	public void dispatch(OptionalDispatcher<T> dispatcher) {
		if (isPresent()) {
			dispatcher.present(reference);
		} else {
			dispatcher.notPresent();
		}
	}

}
