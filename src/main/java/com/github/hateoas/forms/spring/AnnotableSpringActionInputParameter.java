package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.StringOptions;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.DataType;
import com.github.hateoas.forms.affordance.ParameterType;
import com.github.hateoas.forms.affordance.SimpleSuggest;
import com.github.hateoas.forms.affordance.SuggestType;

public class AnnotableSpringActionInputParameter extends SpringActionInputParameter {

	private RequestBody requestBody;

	private RequestParam requestParam;

	private PathVariable pathVariable;

	private RequestHeader requestHeader;

	private final MethodParameter methodParameter;

	/**
	 * Creates action input parameter.
	 *
	 * @param methodParameter to describe
	 * @param value used during sample invocation
	 * @param conversionService to apply to value
	 * @param name parameter name.
	 */
	public AnnotableSpringActionInputParameter(final MethodParameter methodParameter, final Object value,
			final ConversionService conversionService, final String name) {
		super(name, value, conversionService);
		this.methodParameter = methodParameter;
		Annotation[] annotations = methodParameter.getParameterAnnotations();
		Input inputAnnotation = null;
		Select select = null;
		for (Annotation annotation : annotations) {
			if (RequestBody.class.isInstance(annotation)) {
				requestBody = (RequestBody) annotation;
			} else if (RequestParam.class.isInstance(annotation)) {
				requestParam = (RequestParam) annotation;
			} else if (PathVariable.class.isInstance(annotation)) {
				pathVariable = (PathVariable) annotation;
			} else if (RequestHeader.class.isInstance(annotation)) {
				requestHeader = (RequestHeader) annotation;
			} else if (Input.class.isInstance(annotation)) {
				inputAnnotation = (Input) annotation;
			} else if (Select.class.isInstance(annotation)) {
				select = (Select) annotation;
			}
		}

		/**
		 * Check if annotations indicate that is required, for now only for request params & headers
		 */
		boolean requiredByAnnotations = requestParam != null && requestParam.required()
				|| requestHeader != null && requestHeader.required();

		if (inputAnnotation != null) {
			putInputConstraint(ActionInputParameter.MIN, Integer.MIN_VALUE, inputAnnotation.min());
			putInputConstraint(ActionInputParameter.MAX, Integer.MAX_VALUE, inputAnnotation.max());
			putInputConstraint(ActionInputParameter.MIN_LENGTH, Integer.MIN_VALUE, inputAnnotation.minLength());
			putInputConstraint(ActionInputParameter.MAX_LENGTH, Integer.MAX_VALUE, inputAnnotation.maxLength());
			putInputConstraint(ActionInputParameter.STEP, 0, inputAnnotation.step());
			putInputConstraint(ActionInputParameter.PATTERN, "", inputAnnotation.pattern());
			setReadOnly(!inputAnnotation.editable());

			/**
			 * Check if annotations indicate that is required
			 */
			setRequired(inputAnnotation.required() || requiredByAnnotations);

			excluded = inputAnnotation.exclude();
			readOnly = inputAnnotation.readOnly();
			hidden = inputAnnotation.hidden();
			include = inputAnnotation.include();
			type = ParameterType.INPUT;
		} else {
			setReadOnly(select != null ? !select.editable() : !editable);
			putInputConstraint(ActionInputParameter.REQUIRED, "", requiredByAnnotations);
		}
		if (inputAnnotation == null || inputAnnotation.value() == Type.FROM_JAVA) {
			if (isArrayOrCollection() || isRequestBody()) {
				fieldType = null;
			} else if (DataType.isNumber(getParameterType())) {
				fieldType = Type.NUMBER;
			} else {
				fieldType = Type.TEXT;
			}
		} else {
			fieldType = inputAnnotation.value();
		}
		createResolver(methodParameter, select);
		typeDescriptor = TypeDescriptor.nested(methodParameter, 0);
	}

	public AnnotableSpringActionInputParameter(final MethodParameter methodParameter, final Object value,
			final String name) {
		this(methodParameter, value, DEFAULT_CONVERSION_SERVICE, name);
	}

	/**
	 * Creates new ActionInputParameter with default formatting conversion service.
	 *
	 * @param methodParameter holding metadata about the parameter
	 * @param value during sample method invocation
	 */

	public AnnotableSpringActionInputParameter(final MethodParameter methodParameter, final Object value) {
		this(methodParameter, value, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createResolver(final MethodParameter methodParameter, final Select select) {
		Class<?> parameterType = methodParameter.getNestedParameterType();
		Class<?> nested;
		SuggestType type = SuggestType.INTERNAL;
		if (select != null && select.required()) {
			type = select.type();
			putInputConstraint(ActionInputParameter.REQUIRED, "", true);
		}

		if (select != null && (select.options() != StringOptions.class || !isEnumType(parameterType))) {
			resolver = new OptionsPossibleValuesResolver<Object>(select);
			this.type = ParameterType.SELECT;
		} else if (Enum[].class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(
					SimpleSuggest.wrap(parameterType.getComponentType().getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		} else if (Enum.class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(parameterType.getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		} else if (Collection.class.isAssignableFrom(parameterType)) {
			TypeDescriptor descriptor = TypeDescriptor.nested(methodParameter, 1);
			if (descriptor != null) {
				nested = descriptor.getType();
				if (Enum.class.isAssignableFrom(nested)) {
					resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(nested.getEnumConstants()), type);
					this.type = ParameterType.SELECT;
				}
			}
		}

	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and
	 * the kind of input parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {
		if (isRequestBody()) {
			return requestBody.required();
		} else if (isRequestParam()) {
			return !(isDefined(requestParam.defaultValue()) || !requestParam.required());
		} else if (isRequestHeader()) {
			return !(isDefined(requestHeader.defaultValue()) || !requestHeader.required());
		} else {
			return true;
		}
	}

	/**
	 * Determines default value of request param or request header, if available.
	 *
	 * @return value or null
	 */
	public String getDefaultValue() {
		String ret;
		if (isRequestParam()) {
			ret = isDefined(requestParam.defaultValue()) ? requestParam.defaultValue() : null;
		} else if (isRequestHeader()) {
			ret = !ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue()) ? requestHeader.defaultValue() : null;
		} else {
			ret = null;
		}
		return ret;
	}

	private boolean isDefined(final String defaultValue) {
		return !ValueConstants.DEFAULT_NONE.equals(defaultValue);
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getParameterName() {
		String ret;
		String parameterName = methodParameter.getParameterName();
		if (parameterName == null) {
			methodParameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
			ret = methodParameter.getParameterName();
		} else {
			ret = parameterName;
		}
		return ret;
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

	private boolean isEnumType(final Class<?> parameterType) {
		return Enum[].class.isAssignableFrom(parameterType) || Enum.class.isAssignableFrom(parameterType)
				|| Collection.class.isAssignableFrom(parameterType)
						&& Enum.class.isAssignableFrom(TypeDescriptor.nested(methodParameter, 1).getType());
	}

	@Override
	public boolean isRequestBody() {
		return requestBody != null;
	}

	@Override
	public boolean isRequestParam() {
		return requestParam != null;
	}

	@Override
	public boolean isPathVariable() {
		return pathVariable != null;
	}

	@Override
	public boolean isRequestHeader() {
		return requestHeader != null;
	}

	public boolean isInputParameter() {
		return type == ParameterType.INPUT && requestBody == null && pathVariable == null && requestHeader == null
				&& requestParam == null;
	}

	@Override
	public String getRequestHeaderName() {
		return isRequestHeader() ? requestHeader.value() : null;
	}
}
