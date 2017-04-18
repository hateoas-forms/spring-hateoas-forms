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
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.DataType;
import com.github.hateoas.forms.affordance.ParameterType;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestType;
import com.github.hateoas.forms.spring.ActionParameterTypeImpl.FixedPossibleValuesResolver;

/**
 * Describes a Spring MVC rest services method parameter value with recorded sample call value and input constraints.
 *
 * @author Dietrich Schulten
 */
public abstract class SpringActionInputParameter implements ActionInputParameter {

	private static final List<Suggest<?>> EMPTY_SUGGEST = Collections.emptyList();

	final Object value;

	private Boolean arrayOrCollection = null;

	Suggest<?>[] possibleValues;

	Boolean readOnly;

	ParameterType type = ParameterType.UNKNOWN;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	PossibleValuesResolver<?> resolver = new FixedPossibleValuesResolver(EMPTY_SUGGEST, SuggestType.INTERNAL);

	protected static ConversionService DEFAULT_CONVERSION_SERVICE = new DefaultFormattingConversionService();

	final ConversionService conversionService;

	Type fieldType;

	private final String name;

	Boolean required;

	protected SpringActionInputParameter(final String name, final Object value, final ConversionService conversionService) {
		this.name = name;
		this.conversionService = conversionService;
		this.value = value;
	}

	public static void setDefaultConversionService(final ConversionService conversionService) {
		DEFAULT_CONVERSION_SERVICE = conversionService;
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
		return !getInputConstraints().isEmpty();
	}

	@Override
	public boolean isReadOnly() {
		return readOnly != null ? readOnly : false;
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void setRequired(final boolean required) {
		this.required = required;
	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and the kind of input
	 * parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {
		return required != null ? required : false;
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
		return Collections.emptyMap();
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

}
