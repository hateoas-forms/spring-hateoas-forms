package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import org.springframework.core.convert.TypeDescriptor;

public interface ActionParameterType {

	Annotation[] getAnnotations();

	String getName();

	Class<?> getParameterType();

	Class<?> getNestedParameterType();

	TypeDescriptor nested(int nestingLevel);

	ParameterizedType getParameterizedType();

}
