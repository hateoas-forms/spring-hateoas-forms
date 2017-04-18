package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import org.springframework.core.convert.TypeDescriptor;

public class CustomizableActionParameterType extends ActionParameterTypeImpl {

	private final String name;

	private final Class<?> type;

	private final Annotation[] annotations;

	public CustomizableActionParameterType(final String name, final Class<?> type, final Annotation... annotations) {
		super(name);
		this.name = name;
		this.type = type;
		this.annotations = annotations;
	}

	@Override
	public Annotation[] getAnnotations() {
		return annotations;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getParameterType() {
		return type;
	}

	@Override
	public Class<?> getNestedParameterType() {
		return getParameterType();
	}

	@Override
	public TypeDescriptor nested(final int nestingLevel) {
		return nestingLevel == 0 ? TypeDescriptor.valueOf(type) : null;
	}

	@Override
	public ParameterizedType getParameterizedType() {
		return null;
	}

}
