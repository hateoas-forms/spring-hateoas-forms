package com.github.hateoas.forms.spring.xhtml.beans;

import static com.github.hateoas.forms.spring.AffordanceBuilder.linkTo;
import static com.github.hateoas.forms.spring.AffordanceBuilder.methodOn;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.util.SerializationUtils;

import com.github.hateoas.forms.affordance.Affordance;
import com.github.hateoas.forms.spring.AffordanceBuilder;

public class ItemResource extends Resource<Item> {

	public static final ItemResource DUMMY = new ItemResource(new Item(-1, ""));

	public ItemResource(final Item item) {
		super(clone(item));

		add(linkTo(methodOn(DummyController.class).get(item.getId())).withSelfRel());
		AffordanceBuilder editTransferBuilder = linkTo(methodOn(DummyController.class).edit(item.getId(), item));
		add(editTransferBuilder.withRel(HttpMethod.PUT.toString()));
		AffordanceBuilder deleteTransferBuilder = linkTo(methodOn(DummyController.class).delete(item.getId()));
		add(deleteTransferBuilder.withRel(HttpMethod.DELETE.toString()));
		AffordanceBuilder createItemBuilder = linkTo(methodOn(DummyController.class).create(item));
		add(createItemBuilder.withRel(HttpMethod.POST.toString()));
	}

	private static Item clone(final Item item) {
		return (Item) SerializationUtils.deserialize(SerializationUtils.serialize(item));
	}

	public static ItemResource findById(final int id, final Resources<ItemResource> processedResource) {
		for (ItemResource itemResource : processedResource) {
			if (itemResource.getContent().getId() == id) {
				return itemResource;
			}
		}
		return null;
	}

	public Affordance getMethod(final HttpMethod method) {
		if (method == HttpMethod.POST) {
			getContent().setId(-1);
		}
		return (Affordance) getLink(method.toString());
	}

}
