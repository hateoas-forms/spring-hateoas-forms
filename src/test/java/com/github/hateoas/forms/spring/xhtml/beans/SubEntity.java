package com.github.hateoas.forms.spring.xhtml.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.SuggestType;

public class SubEntity implements Serializable {
	private int key;

	private String name;

	private List<SubItem> multiple;

	private ItemType type;

	private SubSubEntity subEntity;

	@JsonCreator
	public SubEntity(@JsonProperty("key") @Input(value = Type.NUMBER) final int key,
			final @JsonProperty("name") @Input(value = Type.TEXT) String name,
			@JsonProperty("multiple") @Select(options = SubItem.SubItemOptions.class, type = SuggestType.EXTERNAL) final List<SubItem> multiple,
			@JsonProperty("type") @Select final ItemType type, @JsonProperty("subEntity") final SubSubEntity subEntity) {
		this.key = key;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
		this.subEntity = subEntity;
	}

	public int getKey() {
		return key;
	}

	public void setKey(final int key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(final ItemType type) {
		this.type = type;
	}

	public List<SubItem> getMultiple() {
		return multiple;
	}

	public void setMultiple(final List<SubItem> multiple) {
		this.multiple = multiple;
	}

	public SubSubEntity getSubEntity() {
		return subEntity;
	}

	public void setSubEntity(final SubSubEntity subEntity) {
		this.subEntity = subEntity;
	}
}
