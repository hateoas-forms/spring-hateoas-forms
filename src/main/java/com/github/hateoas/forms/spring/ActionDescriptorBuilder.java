package com.github.hateoas.forms.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.hateoas.forms.action.Action;
import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.ActionInputParameterVisitor;

public class ActionDescriptorBuilder {

	static ActionDescriptor createActionDescriptor(final MethodParameters parameters, final Method invokedMethod,
			final Map<String, Object> values, final Object[] arguments, final Map<String, ActionInputParameter> requestParamMap) {

		SpringActionDescriptor actionDescriptor = new SpringActionDescriptor(invokedMethod);
		final Action actionAnnotation = AnnotationUtils.getAnnotation(invokedMethod, Action.class);
		if (actionAnnotation != null) {
			actionDescriptor.setSemanticActionType(actionAnnotation.value());
		}

		// the action descriptor needs to know the param type, value and name
		for (Map.Entry<String, ActionInputParameter> entry : requestParamMap.entrySet()) {
			ActionInputParameter value = entry.getValue();
			if (value != null) {
				final String key = entry.getKey();
				actionDescriptor.addRequestParam(key, value);
				if (!value.isRequestBody()) {
					values.put(key, value.getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> pathVariableMap = getActionInputParameters(PathVariable.class, parameters, arguments);
		for (Map.Entry<String, ActionInputParameter> entry : pathVariableMap.entrySet()) {
			ActionInputParameter actionInputParameter = entry.getValue();
			if (actionInputParameter != null) {
				final String key = entry.getKey();
				actionDescriptor.addPathVariable(key, actionInputParameter);
				if (!actionInputParameter.isRequestBody()) {
					values.put(key, actionInputParameter.getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> requestHeadersMap = getActionInputParameters(RequestHeader.class, parameters, arguments);

		for (Map.Entry<String, ActionInputParameter> entry : requestHeadersMap.entrySet()) {
			ActionInputParameter actionInputParameter = entry.getValue();
			if (actionInputParameter != null) {
				final String key = entry.getKey();
				actionDescriptor.addRequestHeader(key, actionInputParameter);
				if (!actionInputParameter.isRequestBody()) {
					values.put(key, actionInputParameter.getValueFormatted());
				}
			}
		}
		Map<String, ActionInputParameter> requestBodyMap = getActionInputParameters(RequestBody.class, parameters, arguments);
		Assert.state(requestBodyMap.size() < 2, "found more than one request body on " + invokedMethod.getName());
		for (ActionInputParameter value : requestBodyMap.values()) {
			actionDescriptor.setRequestBody(value);
		}

		return actionDescriptor;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param annotation to inspect
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getActionInputParameters(final Class<? extends Annotation> annotation,
			final MethodParameters parameters, final Object... arguments) {

		Map<String, ActionInputParameter> result = new LinkedHashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(annotation)) {
			final int parameterIndex = parameter.getParameterIndex();
			final Object argument;
			if (parameterIndex < arguments.length) {
				argument = arguments[parameterIndex];
			}
			else {
				argument = null;
			}
			result.put(parameter.getParameterName(), new AnnotableSpringActionInputParameter(
					new MethodParameterType(parameter), argument, parameter.getParameterName()));
		}

		return result;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param annotation to inspect
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getDTOActionInputParameters(final MethodParameters parameters,
			final Object... arguments) {

		final Map<String, ActionInputParameter> result = new HashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(DTOParam.class)) {
			final int parameterIndex = parameter.getParameterIndex();
			final Object argument;
			if (parameterIndex < arguments.length) {
				argument = arguments[parameterIndex];
			}
			else {
				argument = null;
			}
			SpringActionDescriptor.recurseBeanCreationParams(parameter.getParameterType(), null, argument, "", new HashSet<String>(),
					new ActionInputParameterVisitor() {

						@Override
						public void visit(final ActionInputParameter inputParameter) {
							result.put(inputParameter.getParameterName(), inputParameter);
						}
					}, new ArrayList<ActionInputParameter>());

		}

		return result;
	}

	static Map<String, ActionInputParameter> getRequestParams(final MethodParameters parameters, final Object[] arguments) {
		// the action descriptor needs to know the param type, value and name
		Map<String, ActionInputParameter> requestParamMap = getActionInputParameters(RequestParam.class, parameters, arguments);
		requestParamMap.putAll(getDTOActionInputParameters(parameters, arguments));
		return requestParamMap;

	}

}
