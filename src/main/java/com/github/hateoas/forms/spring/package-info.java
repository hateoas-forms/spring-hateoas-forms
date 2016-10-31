/**
 * Collaborators which are needed to collect information about affordances. The {@link
 * com.github.hateoas.forms.spring.AffordanceBuilder} serves as starting point for affordance creation. Affordance creation
 * works like this: <ol> <li>Dummy call to {@link com.github.hateoas.forms.spring.AffordanceBuilder#linkTo} methods passes
 * sample arguments for the URI template.</li> <li>The {@code AffordanceBuilder} doesn't create an instance of itself,
 * rather it delegates to {@link com.github.hateoas.forms.spring.AffordanceBuilderFactory} for creation. The {@code
 * AffordanceBuilderFactory} analyzes the request mapping, the sample arguments and the target handler method to create
 * an {@code AffordanceBuilder}. The new {@code AffordanceBuilder} receives: <ul> <li>a {@link
 * com.github.hateoas.forms.affordance.PartialUriTemplate} created from request mapping information with applied sample
 * arguments. Template variables which could not be satisfied are kept as variables, no matter if they are required or
 * optional</li> <li>an {@link com.github.hateoas.forms.affordance.ActionDescriptor} which represents the method that
 * handles requests to the URI template resource</li> </ul> <li>The affordance builder has methods to supply information
 * which is necessary to create a link from it, such as {@link com.github.hateoas.forms.spring.AffordanceBuilder#rel
 * (java.lang.String,
 * java.lang.String...)} and other rfc-5988 link parameters</li> <li>Finally, {@link
 * com.github.hateoas.forms.spring.AffordanceBuilder#build} creates the affordance. As a convenience one can also use
 * {@link com.github.hateoas.forms.spring.AffordanceBuilder#withRel} or {@link com.github.hateoas.forms.spring.AffordanceBuilder#withSelfRel}
 * to create a link with a single rel.</li> </ol>
 */
package com.github.hateoas.forms.spring;

