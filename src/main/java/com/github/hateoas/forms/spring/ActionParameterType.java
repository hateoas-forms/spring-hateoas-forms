package com.github.hateoas.forms.spring;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ParameterType;

public interface ActionParameterType {

	String getName();

	Class<?> getParameterType();

	Class<?> getNestedParameterType();

	TypeDescriptor nested(int nestingLevel);

	ParameterizedType getParameterizedType();

	boolean isSingleValue();

	boolean isArray();

	boolean isArrayOrCollection();

	String getParamName();

	DTOParam getDTOParam();

	Select getSelect();

	RequestHeader getRequestHeader();

	PathVariable getPathVariable();

	RequestParam getRequestParam();

	RequestBody getRequestBody();

	Input getInputAnnotation();

	boolean isRequestHeader();

	boolean isPathVariable();

	boolean isRequestParam();

	boolean isRequestBody();

	boolean isRequired();

	boolean isReadOnly();

	Object getValue(Object currentObject);

	PossibleValuesResolver<?> getResolver();

	Map<String, Object> getInputConstraints();

	ParameterType getType();

	Type getHtmlInputFieldType();

}
