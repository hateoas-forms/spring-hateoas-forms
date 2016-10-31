package com.github.hateoas.forms.affordance;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.hateoas.forms.affordance.BeanUtil;
import com.github.hateoas.forms.spring.sample.test.Person;
import com.github.hateoas.forms.spring.sample.test.Review;

/**
 * Created by Dietrich on 06.12.2015.
 */
public class BeanUtilTest {

	@Test
	public void testGetPropertyPaths() throws Exception {
		List<String> propertyPaths = BeanUtil.getPropertyPaths(Person.class);
		assertEquals("name", propertyPaths.get(0));
	}

	@Test
	public void testGetNestedPropertyPaths() throws Exception {
		List<String> propertyPaths = BeanUtil.getPropertyPaths(Review.class);
		assertEquals("reviewBody", propertyPaths.get(0));
		assertEquals("reviewRating.ratingValue", propertyPaths.get(1));
	}
}