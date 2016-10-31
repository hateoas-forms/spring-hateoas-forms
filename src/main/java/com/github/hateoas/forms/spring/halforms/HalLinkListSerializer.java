package com.github.hateoas.forms.spring.halforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class HalLinkListSerializer extends ContainerSerializer<List<Link>> implements ContextualSerializer {
	private static final long serialVersionUID = -1844788111509966406L;

	private static final String RELATION_MESSAGE_TEMPLATE = "_links.%s.title";

	private final BeanProperty property;

	private final CurieProvider curieProvider;

	private final EmbeddedMapper mapper;

	private final MessageSourceAccessor messageSource;

	private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = new Link("__rel__", "¯\\_(ツ)_/¯");

	public HalLinkListSerializer(final CurieProvider curieProvider, final EmbeddedMapper mapper,
			final MessageSourceAccessor messageSource) {
		this(null, curieProvider, mapper, messageSource);
	}

	public HalLinkListSerializer(final BeanProperty property, final CurieProvider curieProvider, final EmbeddedMapper mapper,
			final MessageSourceAccessor messageSource) {
		super(List.class, false);
		this.property = property;
		this.curieProvider = curieProvider;
		this.mapper = mapper;
		this.messageSource = messageSource;
	}

	@Override
	public void serialize(final List<Link> value, final JsonGenerator jgen, final SerializerProvider provider)
			throws IOException, JsonGenerationException {
		Map<String, List<Object>> sortedLinks = new LinkedHashMap();
		List<Link> links = new ArrayList();

		boolean prefixingRequired = curieProvider != null;
		boolean curiedLinkPresent = false;
		boolean skipCuries = !jgen.getOutputContext().getParent().inRoot();

		Object currentValue = jgen.getCurrentValue();
		if (currentValue instanceof Resources && mapper.hasCuriedEmbed((Resources) currentValue)) {
			curiedLinkPresent = true;
		}
		for (Link link : value) {
			if (!link.equals(CURIES_REQUIRED_DUE_TO_EMBEDS)) {
				String rel = prefixingRequired ? curieProvider.getNamespacedRelFrom(link) : link.getRel();
				if (!link.getRel().equals(rel)) {
					curiedLinkPresent = true;
				}
				if (sortedLinks.get(rel) == null) {
					sortedLinks.put(rel, new ArrayList());
				}
				links.add(link);

				((List) sortedLinks.get(rel)).add(toHalLink(link));
			}
		}
		if (!skipCuries && prefixingRequired && curiedLinkPresent) {
			Object curies = new ArrayList();
			((ArrayList) curies).add(curieProvider.getCurieInformation(new Links(links)));

			sortedLinks.put("curies", (ArrayList) curies);
		}
		TypeFactory typeFactory = provider.getConfig().getTypeFactory();
		JavaType keyType = typeFactory.uncheckedSimpleType(String.class);
		JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
		JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

		MapSerializer serializer = MapSerializer.construct(new String[0], mapType, true, null, provider.findKeySerializer(keyType, null),
				new Jackson2HalModule.OptionalListJackson2Serializer(property), null);

		serializer.serialize(sortedLinks, jgen, provider);
	}

	private HalLink toHalLink(final Link link) {
		String rel = link.getRel();
		String title = getTitle(rel);
		if (title == null) {
			title = getTitle(rel.contains(":") ? rel.substring(rel.indexOf(":") + 1) : rel);
		}
		return new HalLink(link, title);
	}

	private String getTitle(final String localRel) {
		Assert.hasText(localRel, "Local relation must not be null or empty!");
		try {
			return messageSource == null ? null : messageSource.getMessage(String.format("_links.%s.title", new Object[] { localRel }));
		}
		catch (NoSuchMessageException o_O) {
		}
		return null;
	}

	public JsonSerializer<?> createContextual(final SerializerProvider provider, final BeanProperty property) throws JsonMappingException {
		return new HalLinkListSerializer(property, curieProvider, mapper, messageSource);
	}

	@Override
	public JavaType getContentType() {
		return null;
	}

	@Override
	public JsonSerializer<?> getContentSerializer() {
		return null;
	}

	@Override
	public boolean isEmpty(final List<Link> value) {
		return isEmpty(null, value);
	}

	@Override
	public boolean isEmpty(final SerializerProvider provider, final List<Link> value) {
		return value.isEmpty();
	}

	@Override
	public boolean hasSingleElement(final List<Link> value) {
		return value.size() == 1;
	}

	@Override
	protected ContainerSerializer<?> _withValueTypeSerializer(final TypeSerializer vts) {
		return null;
	}
}