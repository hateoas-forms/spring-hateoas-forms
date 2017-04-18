package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

public class MethodParameterType extends ActionParameterTypeImpl {

	private final MethodParameter methodParameter;

	private final Method getter;

	public MethodParameterType(final String paramName, final MethodParameter methodParameter) {
		this(paramName, methodParameter, null);
	}

	public MethodParameterType(final String paramName, final MethodParameter methodParameter, final Method getter) {
		super(paramName);
		this.methodParameter = methodParameter;
		this.getter = getter;
		if (getter != null) {
			this.getter.setAccessible(true);
		}
		doSetValues();
	}

	@Override
	public Object getValue(final Object currentObject) {
		if (getter != null) {
			try {
				if (currentObject != null) {
					return getter.invoke(currentObject, (Object[]) null);
				}
				return null;
			}
			catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.getValue(currentObject);
	}

	@Override
	public Annotation[] getAnnotations() {
		return methodParameter.getParameterAnnotations();
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getName() {
		String parameterName = methodParameter.getParameterName();
		if (parameterName == null) {
			methodParameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
			return methodParameter.getParameterName();
		}
		else {
			return parameterName;
		}
	}

	/**
	 * Type of parameter.
	 *
	 * @return type
	 */
	@Override
	public Class<?> getParameterType() {
		return methodParameter.getParameterType();
	}

	@Override
	public Class<?> getNestedParameterType() {
		return methodParameter.getNestedParameterType();
	}

	@Override
	public TypeDescriptor nested(final int nestingLevel) {
		return TypeDescriptor.nested(methodParameter, nestingLevel);
	}

	@Override
	public ParameterizedType getParameterizedType() {
		Type type = methodParameter.getGenericParameterType();
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
}
