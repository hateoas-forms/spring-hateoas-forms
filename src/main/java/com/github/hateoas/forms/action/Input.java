/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package com.github.hateoas.forms.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to define input characteristics of an input value. E.g. this is useful to specify possible value ranges as in
 * <code>&#64;Input(min=0)</code>, and it can also be used to mark a method parameter as <code>&#64;Input(Type.HIDDEN)</code> when used as a
 * GET parameter for a form.
 * <p>
 * Can also be used to specify input characteristics for bean properties if the input value is an object.
 * </p>
 *
 * @author Dietrich Schulten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
public @interface Input {

	/**
	 * Input type, to set the input type, e.g. hidden, password. With the default type FROM_JAVA the type will be number or text for scalar
	 * values (depending on the parameter type), and null for arrays, collections or beans.
	 *
	 * @return input type
	 */
	Type value() default Type.FROM_JAVA;

	int max() default Integer.MAX_VALUE;

	int min() default Integer.MIN_VALUE;

	int minLength() default Integer.MIN_VALUE;

	int maxLength() default Integer.MAX_VALUE;

	String pattern() default "";

	int step() default 0;

	boolean required() default false;

	/**
	 * Entire parameter is not editable, refers both to single values and to all properties of a bean parameter.
	 *
	 * @return true if the parameter is editable
	 */
	boolean editable() default true;

}
