/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hateoas.forms.affordance;

/**
 * Define the "value" and "text" fields of an object
 */
public interface Suggest<T> {

	Suggest<?>[] EMPTY = new Suggest[0];

	/**
	 * Value to show, it may be a wrapped value of the original object in which we have a valid textField and valueField
	 * 
	 * @return the value
	 */
	T getValue();

	/**
	 * Returns the original value, it may be equals to getValue() or not (i.e. Enums), in this case the real value is returned
	 * 
	 * @param <U> Unwrapped type.
	 * @return the unwrapped value
	 */
	<U> U getUnwrappedValue();

	/**
	 * String representation of the valueField inside value object
	 * 
	 * @return the value as String
	 */
	String getValueAsString();

	/**
	 * Value field name
	 * 
	 * @return the value field
	 */
	String getValueField();

	/**
	 * Text field name
	 * 
	 * @return the text field
	 */
	String getTextField();

	/**
	 * String representation of the textField inside value object
	 * 
	 * @return the text
	 */
	String getText();

}
