package com.github.hateoas.forms.affordance;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SuggestImpl<T> implements Suggest<T> {

	private final T value;
	private final String valueField;
	private final String textField;

	public SuggestImpl(T value) {
		this(value, null, null);
	}

	public SuggestImpl(T value, String valueField, String textField) {
		this.value = value;
		this.valueField = valueField;
		this.textField = textField;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String getValueField() {
		return valueField;
	}

	@Override
	public String getTextField() {
		return textField;
	}

	@Override
	public String getText() {
		if (value != null) {
			try {
				return String.valueOf(getField(textField).get(value));
			} catch (Exception e) {
				throw new IllegalArgumentException("Textfield could not be serialized", e);
			}
		}
		return null;
	}

	@Override
	public String getValueAsString() {
		if (value != null) {
			try {
				if (valueField != null) {
					return String.valueOf(getField(valueField).get(value));
				} else {
					return value.toString();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Valuefield could not be serialized", e);
			}
		}
		return null;
	}

	private Field getField(String name) throws NoSuchFieldException, SecurityException {
		Field field = value.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	public static <T> List<Suggest<T>> wrap(List<T> list, String valueField, String textField) {
		List<Suggest<T>> suggests = new ArrayList<Suggest<T>>(list.size());
		for (T value : list) {
			suggests.add(new SuggestImpl<T>(value, valueField, textField));
		}
		return suggests;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <U> U getUnwrappedValue() {
		if (value instanceof WrappedValue) {
			return (U) ((WrappedValue) value).getValue();
		}
		return (U) value;
	}
}
