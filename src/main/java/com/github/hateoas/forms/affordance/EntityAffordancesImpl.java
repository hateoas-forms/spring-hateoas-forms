package com.github.hateoas.forms.affordance;

import static com.github.hateoas.forms.spring.AffordanceBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.hateoas.forms.spring.AffordanceBuilder;

public class EntityAffordancesImpl implements EntityAffordances {

	@Autowired EntityLinks entityLinks;

	public EntityAffordancesImpl() {}

	@Override
	public AffordanceBuilder[] affordanceForSingleResource(final Identifiable<?> entity) {
		return affordanceForSingleResource(entity, RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT,
				RequestMethod.PATCH);
	}

	@Override
	public AffordanceBuilder[] affordanceForSingleResource(final Identifiable<?> entity, final RequestMethod... methods) {
		LinkBuilder builder = entityLinks.linkForSingleResource(entity);
		Link link = builder.withSelfRel();
		List<AffordanceBuilder> builders = new ArrayList<AffordanceBuilder>();
		for (RequestMethod method : methods) {
			if (method.equals(RequestMethod.PATCH) || method.equals(RequestMethod.PUT)) {
				builders.add(linkTo(link, method, entity));
			} else {
				builders.add(linkTo(link, method));
			}
		}
		return builders.toArray(new AffordanceBuilder[builders.size()]);
	}

	@Override
	public AffordanceBuilder[] affordanceToCollectionResource(final Class<?> type) {
		Link link = entityLinks.linkToCollectionResource(type);
		List<AffordanceBuilder> builders = new ArrayList<AffordanceBuilder>();
		builders.add(linkTo(link, RequestMethod.POST, type));
		return builders.toArray(new AffordanceBuilder[builders.size()]);
	}

}
