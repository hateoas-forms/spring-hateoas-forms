package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.hateoas.forms.PropertyUtils;
import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Options;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.StringOptions;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.DataType;
import com.github.hateoas.forms.affordance.ParameterType;
import com.github.hateoas.forms.affordance.SimpleSuggest;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestType;

public abstract class ActionParameterTypeImpl implements ActionParameterType {

	private static final List<Suggest<?>> EMPTY_SUGGEST = Collections.emptyList();

	final String paramName;

	Class<?> parameterType;

	private boolean isArrayOrCollection;

	private boolean isArray;

	private boolean singleValue;

	private RequestBody requestBody;

	private RequestParam requestParam;

	private PathVariable pathVariable;

	private RequestHeader requestHeader;

	private Input inputAnnotation;

	private Select select;

	private DTOParam dtoParam;

	private final Map<String, Object> inputConstraints = new HashMap<String, Object>();

	private Type fieldType;

	private ParameterType type;

	private boolean readOnly;

	private PossibleValuesResolver<Object> resolver = new FixedPossibleValuesResolver(EMPTY_SUGGEST, SuggestType.INTERNAL);

	private boolean required;

	private static final String[] EMPTY = new String[0];

	public ActionParameterTypeImpl(final String paramName) {
		this.paramName = paramName;
	}

	protected void doSetValues() {
		parameterType = getParameterType();
		isArrayOrCollection = DataType.isArrayOrCollection(parameterType);
		isArray = parameterType.isArray();

		final Annotation[] anns = getAnnotations();
		for (int i = 0; i < anns.length; i++) {
			Annotation annotation = anns[i];
			if (RequestBody.class.isInstance(annotation)) {
				requestBody = (RequestBody) annotation;
			}
			else if (RequestParam.class.isInstance(annotation)) {
				requestParam = (RequestParam) annotation;
			}
			else if (PathVariable.class.isInstance(annotation)) {
				pathVariable = (PathVariable) annotation;
			}
			else if (RequestHeader.class.isInstance(annotation)) {
				requestHeader = (RequestHeader) annotation;
			}
			else if (Input.class.isInstance(annotation)) {
				inputAnnotation = (Input) annotation;
			}
			else if (Select.class.isInstance(annotation)) {
				select = (Select) annotation;
			}
			else if (DTOParam.class.isInstance(annotation)) {
				dtoParam = (DTOParam) annotation;
			}
		}
		singleValue = DataType.isSingleValueType(parameterType) || isArrayOrCollection || select != null;

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

			type = ParameterType.INPUT;
		}
		else {
			setReadOnly(select != null ? !select.editable() : readOnly);
			setRequired(requiredByAnnotations);
		}
		if (inputAnnotation == null || inputAnnotation.value() == Type.FROM_JAVA) {
			if (isArrayOrCollection() || isRequestBody()) {
				fieldType = null;
			}
			else if (DataType.isNumber(getParameterType())) {
				fieldType = Type.NUMBER;
			}
			else {
				fieldType = Type.TEXT;
			}
		}
		else {
			fieldType = inputAnnotation.value();
		}
		createResolver(select);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createResolver(final Select select) {
		Class<?> parameterType = getNestedParameterType();
		Class<?> nested;
		SuggestType type = SuggestType.INTERNAL;
		if (select != null && select.required()) {
			type = select.type();
			setRequired(true);
		}

		if (select != null && (select.options() != StringOptions.class || !isEnumType(parameterType))) {
			resolver = new OptionsPossibleValuesResolver<Object>(select);
			this.type = ParameterType.SELECT;
		}
		else if (Enum[].class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(parameterType.getComponentType().getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		}
		else if (Enum.class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(parameterType.getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		}
		else if (Collection.class.isAssignableFrom(parameterType)) {
			TypeDescriptor descriptor = nested(1);
			if (descriptor != null) {
				nested = descriptor.getType();
				if (Enum.class.isAssignableFrom(nested)) {
					resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(nested.getEnumConstants()), type);
					this.type = ParameterType.SELECT;
				}
			}
		}

	}

	private boolean isEnumType(final Class<?> parameterType) {
		return Enum[].class.isAssignableFrom(parameterType) || Enum.class.isAssignableFrom(parameterType)
				|| Collection.class.isAssignableFrom(parameterType) && Enum.class.isAssignableFrom(nested(1).getType());
	}

	protected void putInputConstraint(final String key, final Object defaultValue, final Object value) {
		if (!value.equals(defaultValue)) {
			inputConstraints.put(key, value);
		}
	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and the kind of input
	 * parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
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

	abstract Annotation[] getAnnotations();

	@Override
	public boolean isArrayOrCollection() {
		return isArrayOrCollection;
	}

	@Override
	public boolean isArray() {
		return isArray;
	}

	@Override
	public boolean isSingleValue() {
		return singleValue;
	}

	@Override
	public String getParamName() {
		return paramName;
	}

	@Override
	public RequestBody getRequestBody() {
		return requestBody;
	}

	@Override
	public RequestParam getRequestParam() {
		return requestParam;
	}

	@Override
	public PathVariable getPathVariable() {
		return pathVariable;
	}

	@Override
	public RequestHeader getRequestHeader() {
		return requestHeader;
	}

	@Override
	public Select getSelect() {
		return select;
	}

	@Override
	public Input getInputAnnotation() {
		return inputAnnotation;
	}

	@Override
	public DTOParam getDTOParam() {
		return dtoParam;
	}

	private boolean isDefined(final String defaultValue) {
		return !ValueConstants.DEFAULT_NONE.equals(defaultValue);
	}

	private void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	private void setRequired(final boolean required) {
		this.required = required;
	}

	public class OptionsPossibleValuesResolver<T> implements PossibleValuesResolver<T> {
		private final Options<T> options;

		private final Select select;

		private SuggestType type;

		@SuppressWarnings("unchecked")
		public OptionsPossibleValuesResolver(final Select select) {
			this.select = select;
			setType(select.type());
			options = getOptions((Class<Options<T>>) select.options());
		}

		@Override
		public String[] getParams() {
			return select.args();
		}

		@Override
		public List<Suggest<T>> getValues(final List<?> args) {
			return options.get(select.value(), args.toArray());
		}

		@Override
		public SuggestType getType() {
			return type;
		}

		@Override
		public void setType(final SuggestType type) {
			this.type = type;
		}
	}

	public static class FixedPossibleValuesResolver<T> implements PossibleValuesResolver<T> {

		private final List<Suggest<T>> values;

		private SuggestType type;

		public FixedPossibleValuesResolver(final List<Suggest<T>> values, final SuggestType type) {
			this.values = values;
			setType(type);
		}

		@Override
		public String[] getParams() {
			return EMPTY;
		}

		@Override
		public List<Suggest<T>> getValues(final List<?> value) {
			return values;
		}

		@Override
		public SuggestType getType() {
			return type;
		}

		@Override
		public void setType(final SuggestType type) {
			this.type = type;
		}

	}

	private static <T extends Options<V>, V> Options<V> getOptions(final Class<? extends Options<V>> beanType) {
		Options<V> options = getBean(beanType);
		if (options == null) {
			try {
				options = beanType.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return options;
	}

	private static <T> T getBean(final Class<T> beanType) {
		try {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();

			WebApplicationContext context = WebApplicationContextUtils
					.getWebApplicationContext(servletRequest.getSession().getServletContext());
			Map<String, T> beans = context.getBeansOfType(beanType);
			if (!beans.isEmpty()) {
				return beans.values().iterator().next();
			}
		}
		catch (Exception e) {
		}
		return null;
	}

	private boolean isInputParameter() {
		return type == ParameterType.INPUT && requestBody == null && pathVariable == null && requestHeader == null && requestParam == null;
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
		}
		else if (isRequestHeader()) {
			ret = !ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue()) ? requestHeader.defaultValue() : null;
		}
		else {
			ret = null;
		}
		return ret;
	}

	@Override
	public Object getValue(final Object currentObject) {
		return PropertyUtils.getBeanPropertyValue(currentObject, paramName);
	}

	@Override
	public PossibleValuesResolver<?> getResolver() {
		return resolver;
	}

	@Override
	public Map<String, Object> getInputConstraints() {
		return inputConstraints;
	}

	@Override
	public ParameterType getType() {
		return type;
	}

	/**
	 * Gets HTML5 parameter type for input field according to {@link Type} annotation.
	 *
	 * @return the type
	 */
	@Override
	public Type getHtmlInputFieldType() {
		return fieldType;
	}
}
