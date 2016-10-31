package com.github.hateoas.forms.spring.xhtml.beans;

import java.util.HashMap;

public class BooleanMap extends HashMap<ItemParams, java.lang.Boolean> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1910229391107494412L;

	@Override
	public Boolean get(final Object key) {
		if (containsKey(key)) {
			return super.get(key);
		}
		return Boolean.FALSE;
	}
}
