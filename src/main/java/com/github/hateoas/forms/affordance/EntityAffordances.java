package com.github.hateoas.forms.affordance;

import org.springframework.hateoas.Identifiable;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.hateoas.forms.spring.AffordanceBuilder;

public interface EntityAffordances {

	AffordanceBuilder[] affordanceForSingleResource(final Identifiable<?> entity);

	AffordanceBuilder[] affordanceForSingleResource(final Identifiable<?> entity, final RequestMethod... methods);

	AffordanceBuilder[] affordanceToCollectionResource(final Class<?> type);
}
