package com.github.hateoas.forms.spring;

import java.util.Map;

import org.springframework.core.convert.TypeDescriptor;

public class AnnotableSpringActionInputParameter extends SpringActionInputParameter {

	private final ActionParameterType parameter;

	TypeDescriptor typeDescriptor;

	/**
	 * Creates action input parameter.
	 *
	 * @param parameter to describe
	 * @param value used during sample invocation
	 * @param conversionService to apply to value
	 * @param name parameter name.
	 */
	public AnnotableSpringActionInputParameter(final ActionParameterType parameter, final Object value, final String name) {
		super(name, value, DEFAULT_CONVERSION_SERVICE);
		this.parameter = parameter;
		resolver = parameter.getResolver();
		type = parameter.getType();
		fieldType = parameter.getHtmlInputFieldType();
	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and the kind of input
	 * parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {
		return required != null ? required : parameter.isRequired();
	}

	@Override
	public boolean isReadOnly() {
		return readOnly != null ? readOnly : parameter.isReadOnly();
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getParameterName() {
		return parameter.getName();
	}

	/**
	 * Type of parameter.
	 *
	 * @return type
	 */
	@Override
	public Class<?> getParameterType() {
		return parameter.getParameterType();
	}

	/**
	 * The value of the parameter at sample invocation time, formatted according to conversion configuration.
	 *
	 * @return value, may be null
	 */
	@Override
	public String getValueFormatted() {
		String ret;
		if (value == null) {
			ret = null;
		}
		else {
			ret = (String) conversionService.convert(value, getTypeDescriptor(), TypeDescriptor.valueOf(String.class));
		}
		return ret;
	}

	private TypeDescriptor getTypeDescriptor() {
		if (typeDescriptor == null) {
			typeDescriptor = parameter.nested(0);
		}
		return typeDescriptor;
	}

	@Override
	public boolean isRequestBody() {
		return parameter.isRequestBody();
	}

	@Override
	public boolean isRequestHeader() {
		return parameter.isRequestHeader();
	}

	@Override
	public boolean isRequestParam() {
		return parameter.isRequestParam();
	}

	@Override
	public boolean isPathVariable() {
		return parameter.isPathVariable();
	}

	/**
	 * Determines if action input parameter is an array or collection.
	 *
	 * @return true if array or collection
	 */
	@Override
	public boolean isArrayOrCollection() {
		return parameter.isArrayOrCollection();
	}

	/**
	 * Gets the input constraints defined for this action input parameter.
	 *
	 * @return constraints
	 */
	@Override
	public Map<String, Object> getInputConstraints() {
		return parameter.getInputConstraints();
	}
}
