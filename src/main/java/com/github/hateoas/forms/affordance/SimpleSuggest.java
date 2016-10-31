package com.github.hateoas.forms.affordance;

import java.util.ArrayList;
import java.util.List;

public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper<T>> {

	@SuppressWarnings("unchecked")
	public SimpleSuggest(String text, String value) {
		this(text, value, (T) value);
	}

	public SimpleSuggest(String text, String svalue, T value) {
		this(new SuggestObjectWrapper<T>(text, svalue, value));
	}

	public SimpleSuggest(SuggestObjectWrapper<T> wrapper) {
		super(wrapper, SuggestObjectWrapper.ID, SuggestObjectWrapper.TEXT);
	}

	public static <T> List<Suggest<SuggestObjectWrapper<T>>> wrap(T[] values) {
		List<Suggest<SuggestObjectWrapper<T>>> suggests = new ArrayList<Suggest<SuggestObjectWrapper<T>>>(values.length);
		for (int i = 0; i < values.length; i++) {
			suggests.add(new SimpleSuggest<T>(
					new SuggestObjectWrapper<T>(String.valueOf(values[i]), String.valueOf(values[i]), values[i])));
		}
		return suggests;
	}

}
