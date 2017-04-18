package com.github.hateoas.forms.spring;

public class CustomizableSpringActionInputParameter extends SpringActionInputParameter {

	private final Class<?> parameterType;

	public CustomizableSpringActionInputParameter(final String name, final Object value) {
		super(name, value, DEFAULT_CONVERSION_SERVICE);
		parameterType = value.getClass();
	}

	public CustomizableSpringActionInputParameter(final String name, final Class<?> type) {
		super(name, null, DEFAULT_CONVERSION_SERVICE);
		parameterType = type;
	}

	@Override
	public boolean isRequestBody() {
		return true;
	}

	@Override
	public boolean isRequestHeader() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestParam() {
		return false;
	}

	@Override
	public boolean isPathVariable() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getParameterName() {
		return getName();
	}

	@Override
	public Class<?> getParameterType() {
		return parameterType;
	}

}
