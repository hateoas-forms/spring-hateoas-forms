package com.github.hateoas.forms.spring.xhtml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import com.github.hateoas.forms.spring.sample.test.Event;

public class XhtmlResourceMessageConverterReadTest {

	XhtmlResourceMessageConverter converter = new XhtmlResourceMessageConverter();

	@Test
	public void testRecursivelyCreateObjectNestedBean() throws Exception {
		LinkedMultiValueMap<String, String> formValues = new LinkedMultiValueMap<String, String>();
		formValues.add("workPerformed.name", "foo");
		formValues.add("location", "Harmonie Heilbronn");
		Event event = (Event) converter.recursivelyCreateObject(Event.class, formValues, "");
		assertEquals("foo", event.getWorkPerformed().getContent().name);
		assertEquals("Harmonie Heilbronn", event.location);
	}
}