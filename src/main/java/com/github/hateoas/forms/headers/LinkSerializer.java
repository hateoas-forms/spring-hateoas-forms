package com.github.hateoas.forms.headers;

import java.util.List;

import org.springframework.hateoas.Link;

public interface LinkSerializer {
	List<String> serialize(List<Link> links);
}
