package com.github.hateoas.forms.spring.halforms;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

class HalLink {

	private final Link link;

	private final String title;

	public HalLink(final Link link, final String title) {
		this.link = link;
		this.title = title;
	}

	@JsonUnwrapped
	public Link getLink() {
		return link;
	}

	@JsonInclude(Include.NON_NULL)
	public String getTitle() {
		return title;
	}
}
