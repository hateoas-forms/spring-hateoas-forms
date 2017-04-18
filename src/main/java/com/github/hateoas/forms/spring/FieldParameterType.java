package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.springframework.core.convert.TypeDescriptor;

public class FieldParameterType extends ActionParameterTypeImpl {

	private final Field field;

	public FieldParameterType(final String paramName, final Field field) {
		super(paramName);
		this.field = field;
		doSetValues();
	}

	@Override
	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getName() {
		return field.getName();
	}

	/**
	 * Type of parameter.
	 *
	 * @return type
	 */
	@Override
	public Class<?> getParameterType() {
		return field.getType();
	}

	@Override
	public Class<?> getNestedParameterType() {
		return getParameterType();
	}

	@Override
	public TypeDescriptor nested(final int nestingLevel) {
		return TypeDescriptor.nested(field, nestingLevel);
	}

	@Override
	public ParameterizedType getParameterizedType() {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			return (ParameterizedType) type;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActionParameterType [");
		if (getAnnotations() != null) {
			builder.append("getAnnotations()=");
			builder.append(Arrays.toString(getAnnotations()));
			builder.append(", ");
		}
		if (getName() != null) {
			builder.append("getName()=");
			builder.append(getName());
			builder.append(", ");
		}
		if (getParameterType() != null) {
			builder.append("getParameterType()=");
			builder.append(getParameterType());
			builder.append(", ");
		}
		if (getNestedParameterType() != null) {
			builder.append("getNestedParameterType()=");
			builder.append(getNestedParameterType());
			builder.append(", ");
		}
		if (getParameterizedType() != null) {
			builder.append("getParameterizedType()=");
			builder.append(getParameterizedType());
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Object getValue(final Object currentObject) {
		try {
			if (currentObject != null) {
				return field.get(currentObject);
			}
			return null;
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return super.getValue(currentObject);
	}
}
