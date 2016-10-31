/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.hateoas.forms.spring;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.PartialUriTemplate;

/**
 * Factory for {@link AffordanceBuilder}s in a Spring MVC rest service. Normally one should use the static methods of
 * AffordanceBuilder to get an AffordanceBuilder. Created by dschulten on 03.10.2014.
 */
public class AffordanceBuilderFactory implements MethodLinkBuilderFactory<AffordanceBuilder> {

	private static final MappingDiscoverer MAPPING_DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Override
	public AffordanceBuilder linkTo(final Method method, final Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	@Override
	public AffordanceBuilder linkTo(final Class<?> controller, final Method method, final Object... parameters) {

		String pathMapping = MAPPING_DISCOVERER.getMapping(controller, method);

		final Set<String> params = getRequestParamNames(method, parameters);
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(
				AffordanceBuilder.getBuilder().build().toString() + mapping);

		Map<String, Object> values = new HashMap<String, Object>();

		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}

		ActionDescriptor actionDescriptor = ActionDescriptorBuilder.createActionDescriptor(method, values, parameters);

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	public AffordanceBuilder linkTo(final Link path, final RequestMethod method, final Object body) {
		return linkTo(path, method,
				body != null ? new CustomizableSpringActionInputParameter(body.getClass().getSimpleName(), body) : null);
	}

	public AffordanceBuilder linkTo(final Link path, final RequestMethod method, final Class<?> type) {
		return linkTo(path, method,
				type != null ? new CustomizableSpringActionInputParameter(type.getSimpleName(), type) : null);
	}

	private AffordanceBuilder linkTo(final Link path, final RequestMethod method,
			final CustomizableSpringActionInputParameter parameter) {
		String pathMapping = path.getHref();
		List<String> params = path.getVariableNames();
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";
		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(
				AffordanceBuilder.getBuilder().build().toString() + mapping);

		SpringActionDescriptor actionDescriptor = new SpringActionDescriptor(method.name().toLowerCase(), method.name());

		if (parameter != null) {
			actionDescriptor.setRequestBody(parameter);
		}

		return new AffordanceBuilder(partialUriTemplate.expand(Collections.emptyMap()),
				Collections.singletonList((ActionDescriptor) actionDescriptor));
	}

	private String join(final Collection<String> params) {
		StringBuilder sb = new StringBuilder();
		for (String param : params) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(param);
		}
		return sb.toString();
	}

	@Override
	public AffordanceBuilder linkTo(final Class<?> target) {
		return linkTo(target, new Object[0]);
	}

	@Override
	public AffordanceBuilder linkTo(final Class<?> controller, final Object... parameters) {
		Assert.notNull(controller);

		String mapping = MAPPING_DISCOVERER.getMapping(controller);

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}
		return new AffordanceBuilder().slash(partialUriTemplate.expand(values));
	}

	@Override
	public AffordanceBuilder linkTo(final Class<?> controller, final Map<String, ?> parameters) {
		String mapping = MAPPING_DISCOVERER.getMapping(controller);
		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);
		return new AffordanceBuilder().slash(partialUriTemplate.expand(parameters));
	}

	@Override
	public AffordanceBuilder linkTo(final Object invocationValue) {

		Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, invocationValue);
		DummyInvocationUtils.LastInvocationAware invocations = (DummyInvocationUtils.LastInvocationAware) invocationValue;

		DummyInvocationUtils.MethodInvocation invocation = invocations.getLastInvocation();
		Method invokedMethod = invocation.getMethod();

		String pathMapping = MAPPING_DISCOVERER.getMapping(invokedMethod);

		Set<String> params = getRequestParamNames(invokedMethod, invocation.getArguments());
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(
				AffordanceBuilder.getBuilder().build().toString() + mapping);

		Iterator<Object> classMappingParameters = invocations.getObjectParameters();

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), classMappingParameters.next());
		}

		for (Object argument : invocation.getArguments()) {
			if (names.hasNext()) {
				values.put(names.next(), argument);
			}
		}

		ActionDescriptor actionDescriptor = ActionDescriptorBuilder.createActionDescriptor(invocation.getMethod(), values,
				invocation.getArguments());

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	private Set<String> getRequestParamNames(final Method invokedMethod, final Object[] arguments) {
		return ActionDescriptorBuilder.getRequestParams(invokedMethod, arguments).keySet();
	}
}
