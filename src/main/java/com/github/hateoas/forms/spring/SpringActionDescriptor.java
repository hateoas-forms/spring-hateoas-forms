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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.hateoas.forms.PropertyUtils;
import com.github.hateoas.forms.action.Action;
import com.github.hateoas.forms.action.Cardinality;
import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.action.ResourceHandler;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.ActionInputParameterVisitor;
import com.github.hateoas.forms.affordance.DataType;

/**
 * Describes an HTTP method independently of a specific rest framework. Has knowledge about possible request data, i.e. which types and
 * values are suitable for an action. For example, an action descriptor can be used to create a form with select options and typed input
 * fields that calls a POST handler. It has {@link ActionInputParameter}s which represent method handler arguments. Supported method handler
 * arguments are:
 * <ul>
 * <li>path variables</li>
 * <li>request params (url query params)</li>
 * <li>request headers</li>
 * <li>request body</li>
 * </ul>
 *
 * @author Dietrich Schulten
 */
public class SpringActionDescriptor implements ActionDescriptor {

	private final String httpMethod;

	private final String actionName;

	private final String consumes;

	private final String produces;

	private String semanticActionType;

	private final Map<String, ActionInputParameter> requestParams = new LinkedHashMap<String, ActionInputParameter>();

	private final Map<String, ActionInputParameter> pathVariables = new LinkedHashMap<String, ActionInputParameter>();

	private final Map<String, ActionInputParameter> requestHeaders = new LinkedHashMap<String, ActionInputParameter>();

	private ActionInputParameter requestBody;

	private final Map<String, ActionInputParameter> bodyInputParameters = new LinkedHashMap<String, ActionInputParameter>();

	private Cardinality cardinality = Cardinality.SINGLE;

	/**
	 * Creates an {@link ActionDescriptor}.
	 *
	 * @param actionName name of the action, e.g. the method name of the handler method. Can be used by an action representation, e.g. to
	 * identify the action using a form name.
	 * @param httpMethod used during submit
	 */
	public SpringActionDescriptor(final String actionName, final String httpMethod) {
		this(actionName, httpMethod, null, null);
	}

	public SpringActionDescriptor(final String actionName, final String httpMethod, final String consumes, final String produces) {
		Assert.notNull(actionName);
		Assert.notNull(httpMethod);
		this.httpMethod = httpMethod;
		this.actionName = actionName;
		this.consumes = consumes;
		this.produces = produces;
	}

	public SpringActionDescriptor(final Method method) {
		RequestMethod requestMethod = getHttpMethod(method);
		httpMethod = requestMethod.name();
		actionName = method.getName();
		consumes = getConsumes(method);
		produces = getProduces(method);
		cardinality = getCardinality(method, requestMethod, method.getReturnType());
	}

	/**
	 * The name of the action, for use as form name, usually the method name of the handler method.
	 *
	 * @return action name, never null
	 */
	@Override
	public String getActionName() {
		return actionName;
	}

	/**
	 * Gets the http method of this action.
	 *
	 * @return method, never null
	 */
	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	@Override
	public String getConsumes() {
		return consumes;
	}

	@Override
	public String getProduces() {
		return produces;
	}

	/**
	 * Gets the path variable names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getPathVariableNames() {
		return pathVariables.keySet();
	}

	/**
	 * Gets the request header names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getRequestHeaderNames() {
		return requestHeaders.keySet();
	}

	/**
	 * Gets the request parameter (query param) names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getRequestParamNames() {
		return requestParams.keySet();
	}

	/**
	 * Adds descriptor for request param.
	 *
	 * @param key name of request param
	 * @param actionInputParameter descriptor
	 */
	public void addRequestParam(final String key, final ActionInputParameter actionInputParameter) {
		requestParams.put(key, actionInputParameter);
	}

	/**
	 * Adds descriptor for path variable.
	 *
	 * @param key name of path variable
	 * @param actionInputParameter descriptorg+ann#2
	 */

	public void addPathVariable(final String key, final ActionInputParameter actionInputParameter) {
		pathVariables.put(key, actionInputParameter);
	}

	/**
	 * Adds descriptor for request header.
	 *
	 * @param key name of request header
	 * @param actionInputParameter descriptor
	 */
	public void addRequestHeader(final String key, final ActionInputParameter actionInputParameter) {
		requestHeaders.put(key, actionInputParameter);
	}

	/**
	 * Gets input parameter info which is part of the URL mapping, be it request parameters, path variables or request body attributes.
	 *
	 * @param name to retrieve
	 * @return parameter descriptor or null
	 */
	@Override
	public ActionInputParameter getActionInputParameter(final String name) {
		ActionInputParameter ret = requestParams.get(name);
		if (ret == null) {
			ret = pathVariables.get(name);
		}
		if (ret == null) {
			ret = bodyInputParameters.get(name);
		}
		return ret;
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 *
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	PropertyDescriptor getPropertyDescriptorForPropertyPath(final String propertyPath, final Class<?> propertyType) {
		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
		// Handle nested properties recursively.
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(propertyType, nestedProperty);
			// BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return getPropertyDescriptorForPropertyPath(nestedPath, propertyDescriptor.getPropertyType());
		}
		else {
			return BeanUtils.getPropertyDescriptor(propertyType, propertyPath);
		}
	}

	/**
	 * Gets request header info.
	 *
	 * @param name of the request header
	 * @return request header descriptor or null
	 */
	public ActionInputParameter getRequestHeader(final String name) {
		return requestHeaders.get(name);
	}

	/**
	 * Gets request body info.
	 *
	 * @return request body descriptor or null
	 */
	@Override
	public ActionInputParameter getRequestBody() {
		return requestBody;
	}

	/**
	 * Determines if this descriptor has a request body.
	 *
	 * @return true if request body is present
	 */
	@Override
	public boolean hasRequestBody() {
		return requestBody != null;
	}

	/**
	 * Allows to set request body descriptor.
	 *
	 * @param requestBody descriptor to set
	 */
	public void setRequestBody(final ActionInputParameter requestBody) {
		this.requestBody = requestBody;
		if (requestBody != null) {
			List<ActionInputParameter> bodyInputParameters = new ArrayList<ActionInputParameter>();
			recurseBeanCreationParams(getRequestBody().getParameterType(), (SpringActionInputParameter) getRequestBody(),
					getRequestBody().getValue(), "", Collections.<String> emptySet(), new ActionInputParameterVisitor() {

						@Override
						public void visit(final ActionInputParameter inputParameter) {
						}
					}, bodyInputParameters);
			for (ActionInputParameter actionInputParameter : bodyInputParameters) {
				this.bodyInputParameters.put(actionInputParameter.getName(), actionInputParameter);
			}
		}
	}

	/**
	 * Gets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action. Use {@link Action} on a method handler to define
	 * the semantic type of an action.
	 *
	 * @return URL identifying the type
	 */
	@Override
	public String getSemanticActionType() {
		return semanticActionType;
	}

	/**
	 * Sets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action.
	 *
	 * @param semanticActionType URL identifying the type
	 */
	public void setSemanticActionType(final String semanticActionType) {
		this.semanticActionType = semanticActionType;
	}

	/**
	 * Determines action input parameters for required url variables.
	 *
	 * @return required url variables
	 */
	@Override
	public Map<String, ActionInputParameter> getRequiredParameters() {
		Map<String, ActionInputParameter> ret = new HashMap<String, ActionInputParameter>();
		for (Map.Entry<String, ActionInputParameter> entry : requestParams.entrySet()) {
			ActionInputParameter annotatedParameter = entry.getValue();
			if (annotatedParameter.isRequired()) {
				ret.put(entry.getKey(), annotatedParameter);
			}
		}
		for (Map.Entry<String, ActionInputParameter> entry : pathVariables.entrySet()) {
			ActionInputParameter annotatedParameter = entry.getValue();
			ret.put(entry.getKey(), annotatedParameter);
		}
		// requestBody not supported, would have to use exploded modifier
		return ret;
	}

	/**
	 * Allows to set the cardinality, i.e. specify if the action refers to a collection or a single resource. Default is
	 * {@link Cardinality#SINGLE}
	 *
	 * @param cardinality to set
	 */
	public void setCardinality(final Cardinality cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Allows to decide whether or not the action refers to a collection resource.
	 *
	 * @return cardinality
	 */
	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public void accept(final ActionInputParameterVisitor visitor) {
		if (hasRequestBody()) {
			for (ActionInputParameter inputParameter : bodyInputParameters.values()) {
				visitor.visit(inputParameter);
			}
		}
		else {
			Collection<String> paramNames = getRequestParamNames();
			for (String paramName : paramNames) {
				ActionInputParameter inputParameter = getActionInputParameter(paramName);
				visitor.visit(inputParameter);
			}
		}

	}

	/**
	 * Renders input fields for bean properties of bean to add or update or patch.
	 *
	 * @param sirenFields to add to
	 * @param beanType to render
	 * @param annotatedParameters which describes the method
	 * @param annotatedParameter which requires the bean
	 * @param currentCallValue sample call value
	 */
	static void recurseBeanCreationParams(final Class<?> beanType, final SpringActionInputParameter annotatedParameter,
			final Object currentCallValue, final String parentParamName, final Set<String> knownFields,
			final ActionInputParameterVisitor methodHandler, final List<ActionInputParameter> bodyInputParameters) {

		// TODO collection, map and object node creation are only describable by an annotation, not via type reflection
		if (ObjectNode.class.isAssignableFrom(beanType) || Map.class.isAssignableFrom(beanType)
				|| Collection.class.isAssignableFrom(beanType) || beanType.isArray()) {
			return; // use @Input(include) to list parameter names, at least? Or mix with hdiv's form builder?
		}
		try {
			Constructor<?>[] constructors = beanType.getConstructors();
			// find default ctor
			Constructor<?> constructor = PropertyUtils.findDefaultCtor(constructors);
			// find ctor with JsonCreator ann
			if (constructor == null) {
				constructor = PropertyUtils.findJsonCreator(constructors, JsonCreator.class);
			}
			Assert.notNull(constructor, "no default constructor or JsonCreator found for type " + beanType.getName());
			int parameterCount = constructor.getParameterTypes().length;

			Set<String> knownConstructorFields = new HashSet<String>();
			if (parameterCount > 0) {
				Annotation[][] annotationsOnParameters = constructor.getParameterAnnotations();

				Class<?>[] parameters = constructor.getParameterTypes();
				int paramIndex = 0;
				for (Annotation[] annotationsOnParameter : annotationsOnParameters) {
					for (Annotation annotation : annotationsOnParameter) {
						if (JsonProperty.class == annotation.annotationType()) {
							JsonProperty jsonProperty = (JsonProperty) annotation;

							// TODO use required attribute of JsonProperty for required fields ->
							String paramName = jsonProperty.value();
							Class<?> parameterType = parameters[paramIndex];
							Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, paramName);
							MethodParameter methodParameter = new MethodParameter(constructor, paramIndex);

							String fieldName = invokeHandlerOrFollowRecurse(methodParameter, annotatedParameter, parentParamName, paramName,
									parameterType, propertyValue, knownConstructorFields, methodHandler, bodyInputParameters);

							if (fieldName != null) {
								knownConstructorFields.add(fieldName);
							}

							paramIndex++; // increase for each @JsonProperty
						}
					}
				}
				Assert.isTrue(parameters.length == paramIndex,
						"not all constructor arguments of @JsonCreator " + constructor.getName() + " are annotated with @JsonProperty");
			}

			// TODO support Option provider by other method args?
			final BeanInfo beanInfo = Introspector.getBeanInfo(beanType);
			final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

			// add input field for every setter
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				final Method writeMethod = propertyDescriptor.getWriteMethod();
				String propertyName = propertyDescriptor.getName();

				if (writeMethod == null || knownFields.contains(parentParamName + propertyName)) {
					continue;
				}
				final Class<?> propertyType = propertyDescriptor.getPropertyType();

				Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, propertyName);
				MethodParameter methodParameter = new MethodParameter(propertyDescriptor.getWriteMethod(), 0);

				invokeHandlerOrFollowRecurse(methodParameter, annotatedParameter, parentParamName, propertyName, propertyType,
						propertyValue, knownConstructorFields, methodHandler, bodyInputParameters);

			}
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to write input fields for constructor", e);
		}
	}

	private static String invokeHandlerOrFollowRecurse(final MethodParameter methodParameter,
			final SpringActionInputParameter annotatedParameter, final String parentParamName, final String paramName,
			final Class<?> parameterType, final Object propertyValue, final Set<String> knownFields,
			final ActionInputParameterVisitor handler, final List<ActionInputParameter> bodyInputParameters) {

		Annotation[] annotations = methodParameter.getParameterAnnotations();
		String paramPath = parentParamName + paramName;
		if (DataType.isSingleValueType(parameterType) || DataType.isArrayOrCollection(parameterType)
				|| getParameterAnnotation(annotations, Select.class) != null) {
			/**
			 * TODO This is a temporal patch, to be reviewed...
			 */
			if (annotatedParameter == null) {
				ActionInputParameter inputParameter = new AnnotableSpringActionInputParameter(methodParameter, propertyValue,
						parentParamName + paramName);
				bodyInputParameters.add(inputParameter);
				handler.visit(inputParameter);
				return inputParameter.getName();
			}
			else if (annotatedParameter.isIncluded(paramPath) && !knownFields.contains(parentParamName + paramName)) {
				DTOParam dtoAnnotation = getParameterAnnotation(annotations, DTOParam.class);
				StringBuilder sb = new StringBuilder(64);
				if (DataType.isArrayOrCollection(parameterType) && dtoAnnotation != null) {
					Object wildCardValue = null;
					if (propertyValue != null) {
						// if the element is wildcard dto type element we need to get the first value
						if (parameterType.isArray()) {
							Object[] array = (Object[]) propertyValue;
							if (!dtoAnnotation.wildcard()) {
								for (int i = 0; i < array.length; i++) {
									if (array[i] != null) {
										sb.setLength(0);
										recurseBeanCreationParams(array[i].getClass(), annotatedParameter, array[i],
												sb.append(parentParamName).append(paramName).append('[').append(i).append("].").toString(),
												knownFields, handler, bodyInputParameters);
									}
								}
							}
							else if (array.length > 0) {
								wildCardValue = array[0];
							}
						}
						else {
							int i = 0;
							if (!dtoAnnotation.wildcard()) {
								for (Object value : (Collection<?>) propertyValue) {
									if (value != null) {
										sb.setLength(0);
										recurseBeanCreationParams(
												value.getClass(), annotatedParameter, value, sb.append(parentParamName).append(paramName)
														.append('[').append(i++).append("].").toString(),
												knownFields, handler, bodyInputParameters);
									}
								}
							}
							else if (!((Collection<?>) propertyValue).isEmpty()) {
								wildCardValue = ((Collection<?>) propertyValue).iterator().next();
							}
						}
					}
					if (dtoAnnotation.wildcard()) {
						Class<?> willCardClass = null;
						if (wildCardValue != null) {
							willCardClass = wildCardValue.getClass();
						}
						else {
							Type type = methodParameter.getGenericParameterType();
							if (type != null && type instanceof ParameterizedType) {
								willCardClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
							}
						}
						if (willCardClass != null) {
							recurseBeanCreationParams(willCardClass, annotatedParameter, wildCardValue,
									sb.append(parentParamName).append(paramName).append(DTOParam.WILDCARD_LIST_MASK).append('.').toString(),
									knownFields, handler, bodyInputParameters);
						}
					}
					return parentParamName + paramName;
				}
				else {
					SpringActionInputParameter inputParameter = new AnnotableSpringActionInputParameter(methodParameter, propertyValue,
							parentParamName + paramName);
					// TODO We need to find a better solution for this
					inputParameter.possibleValues = annotatedParameter.possibleValues;
					bodyInputParameters.add(inputParameter);
					handler.visit(inputParameter);
					if (annotatedParameter.isReadOnly(paramPath)) {
						inputParameter.setReadOnly(true);
					}
					if (annotatedParameter.isHidden(paramPath)) {
						inputParameter.setHtmlInputFieldType(com.github.hateoas.forms.action.Type.HIDDEN);
					}
					return inputParameter.getName();
				}
			}

		}
		else {
			Object callValueBean;
			if (propertyValue instanceof Resource) {
				callValueBean = ((Resource<?>) propertyValue).getContent();
			}
			else {
				callValueBean = propertyValue;
			}
			recurseBeanCreationParams(parameterType, annotatedParameter, callValueBean, parentParamName + paramName + ".", knownFields,
					handler, bodyInputParameters);
		}

		return null;
	}

	private static RequestMethod getHttpMethod(final Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		RequestMethod requestMethod;
		if (methodRequestMapping != null) {
			RequestMethod[] methods = methodRequestMapping.method();
			if (methods.length == 0) {
				requestMethod = RequestMethod.GET;
			}
			else {
				requestMethod = methods[0];
			}
		}
		else {
			requestMethod = RequestMethod.GET; // default
		}
		return requestMethod;
	}

	private static String getConsumes(final Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (methodRequestMapping != null) {
			StringBuilder sb = new StringBuilder();
			for (String consume : methodRequestMapping.consumes()) {
				sb.append(consume).append(",");
			}
			if (sb.length() > 1) {
				sb.setLength(sb.length() - 1);
				return sb.toString();
			}
		}
		return null;
	}

	private static String getProduces(final Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (methodRequestMapping != null) {
			StringBuilder sb = new StringBuilder();
			for (String produce : methodRequestMapping.produces()) {
				sb.append(produce).append(",");
			}
			if (sb.length() > 1) {
				sb.setLength(sb.length() - 1);
				return sb.toString();
			}
		}
		return null;
	}

	private Cardinality getCardinality(final Method invokedMethod, final RequestMethod httpMethod, final Type genericReturnType) {
		Cardinality cardinality;

		ResourceHandler resourceAnn = AnnotationUtils.findAnnotation((AnnotatedElement)invokedMethod, ResourceHandler.class);
		if (resourceAnn != null) {
			cardinality = resourceAnn.value();
		}
		else {
			if (RequestMethod.POST == httpMethod || containsCollection(genericReturnType)) {
				cardinality = Cardinality.COLLECTION;
			}
			else {
				cardinality = Cardinality.SINGLE;
			}
		}
		return cardinality;
	}

	private boolean containsCollection(final Type genericReturnType) {
		final boolean ret;
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType t = (ParameterizedType) genericReturnType;
			Type rawType = t.getRawType();
			Assert.state(rawType instanceof Class<?>, "raw type is not a Class: " + rawType.toString());
			Class<?> cls = (Class<?>) rawType;
			if (HttpEntity.class.isAssignableFrom(cls)) {
				Type[] typeArguments = t.getActualTypeArguments();
				ret = containsCollection(typeArguments[0]);
			}
			else if (Resources.class.isAssignableFrom(cls) || Collection.class.isAssignableFrom(cls)) {
				ret = true;
			}
			else {
				ret = false;
			}
		}
		else if (genericReturnType instanceof GenericArrayType) {
			ret = true;
		}
		else if (genericReturnType instanceof WildcardType) {
			WildcardType t = (WildcardType) genericReturnType;
			ret = containsCollection(getBound(t.getLowerBounds())) || containsCollection(getBound(t.getUpperBounds()));
		}
		else if (genericReturnType instanceof TypeVariable) {
			ret = false;
		}
		else if (genericReturnType instanceof Class) {
			Class<?> cls = (Class<?>) genericReturnType;
			ret = Resources.class.isAssignableFrom(cls) || Collection.class.isAssignableFrom(cls);
		}
		else {
			ret = false;
		}
		return ret;
	}

	private Type getBound(final Type[] lowerBounds) {
		Type ret;
		if (lowerBounds != null && lowerBounds.length > 0) {
			ret = lowerBounds[0];
		}
		else {
			ret = null;
		}
		return ret;
	}

	@Override
	public String toString() {
		return "SpringActionDescriptor [httpMethod=" + httpMethod + ", actionName=" + actionName + "]";
	}

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T getParameterAnnotation(final Annotation[] anns, final Class<T> annotationType) {
		for (Annotation ann : anns) {
			if (annotationType.isInstance(ann)) {
				return (T) ann;
			}
		}
		return null;
	}

}
