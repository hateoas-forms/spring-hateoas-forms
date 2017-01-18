package com.github.hateoas.forms.headers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;

public class MultipleLinkHeaderSerializer implements LinkSerializer {

	@Override
	public List<String> serialize(final List<Link> links) {
		List<String> values = new ArrayList<String>();
		for (Link link : links) {
			values.add(serializeSingleLink(link));
		}
		return values;
	}

	protected String serializeSingleLink(final Link link) {
		return LinkHeaders.createLinkHeader(link.getHref(), link.getRel());
	}
}
