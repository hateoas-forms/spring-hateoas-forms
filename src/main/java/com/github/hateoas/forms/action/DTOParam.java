package com.github.hateoas.forms.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DTOParam {

	public static final String WILDCARD_LIST_MASK = "[*]";

	/**
	 * Set the behavior of the object as wildcard. Its properties will be checked as editable values.
	 * 
	 * @return
	 */
	boolean wildcard() default false;
}
