package com.github.hateoas.forms.spring.halforms;

import java.util.Map;

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.util.Assert;

public class EmbeddedMapper {
	private final RelProvider relProvider;

	private final CurieProvider curieProvider;

	private final boolean preferCollectionRels;

	public EmbeddedMapper(final RelProvider relProvider, final CurieProvider curieProvider, final boolean preferCollectionRels) {
		Assert.notNull(relProvider, "RelProvider must not be null!");

		this.relProvider = relProvider;
		this.curieProvider = curieProvider;
		this.preferCollectionRels = preferCollectionRels;
	}

	public Map<String, Object> map(final Iterable<?> source) {
		Assert.notNull(source, "Elements must not be null!");

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(relProvider, curieProvider, preferCollectionRels);
		for (Object resource : source) {
			builder.add(resource);
		}
		return builder.asMap();
	}

	public boolean hasCuriedEmbed(final Iterable<?> source) {
		for (String rel : map(source).keySet()) {
			if (rel.contains(":")) {
				return true;
			}
		}
		return false;
	}
}
