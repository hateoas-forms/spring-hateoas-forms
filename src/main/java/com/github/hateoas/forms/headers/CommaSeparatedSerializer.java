package com.github.hateoas.forms.headers;

import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.Link;

public class CommaSeparatedSerializer extends MultipleLinkHeaderSerializer {

	@Override
	public List<String> serialize(final List<Link> links) {
		return Collections.singletonList(mergeLinks(super.serialize(links)));
	}

	private String mergeLinks(final List<String> links) {
		StringBuilder sb = new StringBuilder(256);
		for (String string : links) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(string);
		}
		return sb.toString();
	}

}
