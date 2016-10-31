package com.github.hateoas.forms.spring.halforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

class HalEmbeddedBuilder {
	private static final String DEFAULT_REL = "content";

	private static final String INVALID_EMBEDDED_WRAPPER = "Embedded wrapper %s returned null for both the static rel and the rel target type! Make sure one of the two returns a non-null value!";

	private final Map<String, Object> embeddeds = new HashMap();

	private final RelProvider provider;

	private final CurieProvider curieProvider;

	private final EmbeddedWrappers wrappers;

	public HalEmbeddedBuilder(final RelProvider provider, final CurieProvider curieProvider, final boolean preferCollectionRels) {
		Assert.notNull(provider, "Relprovider must not be null!");

		this.provider = provider;
		this.curieProvider = curieProvider;
		wrappers = new EmbeddedWrappers(preferCollectionRels);
	}

	public void add(final Object source) {
		EmbeddedWrapper wrapper = wrappers.wrap(source);
		if (wrapper == null) {
			return;
		}
		String collectionRel = getDefaultedRelFor(wrapper, true);
		String collectionOrItemRel = collectionRel;
		if (!embeddeds.containsKey(collectionRel)) {
			collectionOrItemRel = getDefaultedRelFor(wrapper, wrapper.isCollectionValue());
		}
		Object currentValue = embeddeds.get(collectionOrItemRel);
		Object value = wrapper.getValue();
		if (currentValue == null && !wrapper.isCollectionValue()) {
			embeddeds.put(collectionOrItemRel, value);
			return;
		}
		List<Object> list = new ArrayList();
		list.addAll(asCollection(currentValue));
		list.addAll(asCollection(wrapper.getValue()));

		embeddeds.remove(collectionOrItemRel);
		embeddeds.put(collectionRel, list);
	}

	private Collection<Object> asCollection(final Object source) {
		return source == null ? Collections.emptySet() : source instanceof Collection ? (Collection) source : Collections.singleton(source);
	}

	private String getDefaultedRelFor(final EmbeddedWrapper wrapper, final boolean forCollection) {
		String valueRel = wrapper.getRel();
		if (StringUtils.hasText(valueRel)) {
			return valueRel;
		}
		if (provider == null) {
			return "content";
		}
		Class<?> type = wrapper.getRelTargetType();
		if (type == null) {
			throw new IllegalStateException(String.format(
					"Embedded wrapper %s returned null for both the static rel and the rel target type! Make sure one of the two returns a non-null value!",
					new Object[] { wrapper }));
		}
		String rel = forCollection ? provider.getCollectionResourceRelFor(type) : provider.getItemResourceRelFor(type);
		if (curieProvider != null) {
			rel = curieProvider.getNamespacedRelFor(rel);
		}
		return rel == null ? "content" : rel;
	}

	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(embeddeds);
	}
}
