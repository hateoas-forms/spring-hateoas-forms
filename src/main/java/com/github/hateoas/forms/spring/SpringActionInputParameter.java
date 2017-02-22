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

package com.github.hateoas.forms.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Options;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.DataType;
import com.github.hateoas.forms.affordance.ParameterType;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestType;

/**
 * Describes a Spring MVC rest services method parameter value with recorded sample call value and input constraints.
 *
 * @author Dietrich Schulten
 */
public abstract class SpringActionInputParameter implements ActionInputParameter {

	private static final String[] EMPTY = new String[0];

	private static final List<Suggest<?>> EMPTY_SUGGEST = Collections.emptyList();

	final Object value;

	private Boolean arrayOrCollection = null;

	private final Map<String, Object> inputConstraints = new HashMap<String, Object>();

	Suggest<?>[] possibleValues;

	String[] excluded = EMPTY;

	String[] readOnly = EMPTY;

	String[] hidden = EMPTY;

	String[] include = EMPTY;

	boolean editable = true;

	ParameterType type = ParameterType.UNKNOWN;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	PossibleValuesResolver<?> resolver = new FixedPossibleValuesResolver(EMPTY_SUGGEST, SuggestType.INTERNAL);

	protected static ConversionService DEFAULT_CONVERSION_SERVICE = new DefaultFormattingConversionService();

	final ConversionService conversionService;

	Type fieldType;

	private final String name;

	protected SpringActionInputParameter(final String name, final Object value, final ConversionService conversionService) {
		this.name = name;
		this.conversionService = conversionService;
		this.value = value;
	}

	public static void setDefaultConversionService(final ConversionService conversionService) {
		DEFAULT_CONVERSION_SERVICE = conversionService;
	}

	protected void putInputConstraint(final String key, final Object defaultValue, final Object value) {
		if (!value.equals(defaultValue)) {
			inputConstraints.put(key, value);
		}
	}

	/**
	 * The value of the parameter at sample invocation time.
	 *
	 * @return value, may be null
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * The value of the parameter at sample invocation time, formatted according to conversion configuration.
	 *
	 * @return value, may be null
	 */
	@Override
	public String getValueFormatted() {
		return value.toString();
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

	@Override
	public void setHtmlInputFieldType(final Type type) {
		fieldType = type;
	}

	/**
	 * Has constraints defined via <code>@Input</code> annotation. Note that there might also be other kinds of constraints, e.g.
	 * <code>@Select</code> may define values for {@link #getPossibleValues}.
	 *
	 * @return true if parameter is constrained
	 */
	@Override
	public boolean hasInputConstraints() {
		return !inputConstraints.isEmpty();
	}

	/**
	 * Determines if request body input parameter has a hidden input property.
	 *
	 * @param property name or property path
	 * @return true if hidden
	 */
	boolean isHidden(final String property) {
		return arrayContains(hidden, property);
	}

	boolean isReadOnly(final String property) {
		return !editable || arrayContains(readOnly, property);
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		editable = !readOnly;
		putInputConstraint(ActionInputParameter.EDITABLE, "", editable);
	}

	@Override
	public void setRequired(final boolean required) {
		putInputConstraint(ActionInputParameter.REQUIRED, "", required);
	}

	boolean isIncluded(final String property) {
		if (isExcluded(property)) {
			return false;
		}
		if (include == null || include.length == 0) {
			return true;
		}
		return containsPropertyIncludeValue(property);
	}

	/**
	 * Find out if property is included by searching through all annotations.
	 *
	 * @param property
	 * @return
	 */
	private boolean containsPropertyIncludeValue(final String property) {
		return arrayContains(readOnly, property) || arrayContains(hidden, property) || arrayContains(include, property);
	}

	/**
	 * Determines if request body input parameter should be excluded, considering {@link Input#exclude}.
	 *
	 * @param property name or property path
	 * @return true if excluded, false if no include statement found or not excluded
	 */
	private boolean isExcluded(final String property) {
		return excluded != null && arrayContains(excluded, property);
	}

	private boolean arrayContains(final String[] array, final String toFind) {
		if (array == null || array.length == 0) {
			return false;
		}
		for (String item : array) {
			if (toFind.equals(item)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> List<Suggest<T>> getPossibleValues(final ActionDescriptor actionDescriptor) {
		List<Object> from = new ArrayList<Object>();
		for (String paramName : resolver.getParams()) {
			ActionInputParameter parameterValue = actionDescriptor.getActionInputParameter(paramName);
			if (parameterValue != null) {
				from.add(parameterValue.getValue());
			}
		}

		return (List) resolver.getValues(from);

	}

	@Override
	public <T> void setPossibleValues(final List<Suggest<T>> possibleValues) {
		resolver = new FixedPossibleValuesResolver<T>(possibleValues, resolver.getType());
	}

	@Override
	public SuggestType getSuggestType() {
		return resolver.getType();
	}

	@Override
	public void setSuggestType(final SuggestType type) {
		resolver.setType(type);
	}

	/**
	 * Determines if action input parameter is an array or collection.
	 *
	 * @return true if array or collection
	 */
	@Override
	public boolean isArrayOrCollection() {
		if (arrayOrCollection == null) {
			arrayOrCollection = DataType.isArrayOrCollection(getParameterType());
		}
		return arrayOrCollection;
	}

	/**
	 * Allows convenient access to multiple call values in case that this input parameter is an array or collection. Make sure to check
	 * {@link #isArrayOrCollection()} before calling this method.
	 *
	 * @return call values or empty array
	 * @throws UnsupportedOperationException if this input parameter is not an array or collection
	 */
	@Override
	public Object[] getValues() {
		Object[] callValues;
		if (!isArrayOrCollection()) {
			throw new UnsupportedOperationException("parameter is not an array or collection");
		}
		Object callValue = getValue();
		if (callValue == null) {
			callValues = new Object[0];
		}
		else {
			Class<?> parameterType = getParameterType();
			if (parameterType.isArray()) {
				callValues = (Object[]) callValue;
			}
			else {
				callValues = ((Collection<?>) callValue).toArray();
			}
		}
		return callValues;
	}

	/**
	 * Was a sample call value recorded for this parameter?
	 *
	 * @return if call value is present
	 */
	@Override
	public boolean hasValue() {
		return value != null;
	}

	/**
	 * Gets the input constraints defined for this action input parameter.
	 *
	 * @return constraints
	 */
	@Override
	public Map<String, Object> getInputConstraints() {
		return inputConstraints;
	}

	@Override
	public String toString() {
		String kind;
		if (isRequestBody()) {
			kind = "RequestBody";
		}
		else if (isPathVariable()) {
			kind = "PathVariable";
		}
		else if (isRequestParam()) {
			kind = "RequestParam";
		}
		else if (isRequestHeader()) {
			kind = "RequestHeader";
		}
		else {
			kind = "nested bean property";
		}
		return kind + (getParameterName() != null ? " " + getParameterName() : "") + ": " + (value != null ? value.toString() : "no value");
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

	public void setExcluded(final String[] excluded) {
		this.excluded = excluded;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ParameterType getType() {
		return type;
	}

	public void setType(final ParameterType type) {
		this.type = type;
	}

	interface PossibleValuesResolver<T> {
		String[] getParams();

		List<Suggest<T>> getValues(List<?> value);

		SuggestType getType();

		void setType(SuggestType type);
	}

	class FixedPossibleValuesResolver<T> implements PossibleValuesResolver<T> {

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

	class OptionsPossibleValuesResolver<T> implements PossibleValuesResolver<T> {
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

}
