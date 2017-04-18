package com.github.hateoas.forms.spring;

import java.util.List;

import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestType;

interface PossibleValuesResolver<T> {
	String[] getParams();

	List<Suggest<T>> getValues(List<?> value);

	SuggestType getType();

	void setType(SuggestType type);
}