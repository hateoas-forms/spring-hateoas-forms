package com.github.hateoas.forms.headers;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;

public class LinkHeaders {

	private static LinkSerializer serializer = new CommaSeparatedSerializer();

	public LinkHeaders() {
	}

	public static void setSerializer(final LinkSerializer serializer) {
		LinkHeaders.serializer = serializer;
	}

	public static String createLinkHeader(final String uri, final String rel) {
		return new StringBuilder(32).append('<').append(uri).append(">; rel=\"").append(rel).append('\"').toString();
	}

	public static List<String> toHeaderList(final List<Link> links) {
		return serializer.serialize(links);
	}

	public static HttpHeaders toHttpHeaders(final List<Link> links) {
		return toHttpHeaders(new HttpHeaders(), links);
	}

	public static HttpHeaders toHttpHeaders(final HttpHeaders headers, final List<Link> links) {
		List<String> list = toHeaderList(links);
		for (String string : list) {
			headers.add("Link", string);
		}
		return headers;
	}

}
