package com.github.hateoas.forms.action;

import java.util.Arrays;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.util.Assert;

import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestImpl;
import com.github.hateoas.forms.spring.AffordanceBuilder;

public class RemoteOptions implements Options<String> {

	private final Object lastInvocation;

	private final String idField;

	private final String textField;

	//@formatter:off 
	/**
	 * Usage:
	 * 	public static class SearchableOptions extends RemoteOptions {
     *		public SearchableOptions() {
			super(AffordanceBuilder.methodOn(DummyController.class).searchMethod(....), "id", "name");
	 *	   }
	 *	}
	 * @param lastInvocation
	 * @param idField
	 * @param textField
	 */
	//@formatter:on
	public RemoteOptions(final Object lastInvocation, final String idField, final String textField) {
		Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, lastInvocation);
		this.lastInvocation = lastInvocation;
		this.idField = idField;
		this.textField = textField;
	}

	@Override
	public List<Suggest<String>> get(final String[] value, final Object... args) {
		Link link = AffordanceBuilder.linkTo(lastInvocation).withSelfRel();
		return SuggestImpl.wrap(Arrays.asList(link.getHref()), idField, textField);
	}

	public static List<Suggest<String>> wrap(final String url, final Suggest<String> suggest) {
		return SuggestImpl.wrap(Arrays.asList(url), suggest.getValueField(), suggest.getTextField());
	}
}
