package com.github.hateoas.forms.affordance;

import com.github.hateoas.forms.spring.Path;

public class SuggestObjectWrapper<T> implements WrappedValue<T> {

	public static final String ID = Path.path(Path.on(SuggestObjectWrapper.class).getSvalue());
	public static final String TEXT = Path.path(Path.on(SuggestObjectWrapper.class).getText());

	private final String text;
	private final String svalue;
	private final T original;

	public SuggestObjectWrapper(String text, String id, T original) {
		this.text = text;
		this.svalue = id;
		this.original = original;
	}

	public String getText() {
		return text;
	}

	public String getSvalue() {
		return svalue;
	}

	@Override
	public T getValue() {
		return original;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((svalue == null) ? 0 : svalue.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SuggestObjectWrapper<?> other = (SuggestObjectWrapper<?>) obj;
		if (svalue == null) {
			if (other.svalue != null) {
				return false;
			}
		} else if (!svalue.equals(other.svalue)) {
			return false;
		}
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		} else if (!text.equals(other.text)) {
			return false;
		}
		return true;
	}

}
