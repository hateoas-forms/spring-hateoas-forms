package com.github.hateoas.forms.spring.xhtml.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.SuggestType;

public class SubSubEntity implements Serializable {
	private int key;

	private List<SubItem> multiple;

	private String name;

	private ItemType type;

	@JsonCreator
	public SubSubEntity(@JsonProperty("key") @Input(value = Type.NUMBER) final int key,
			final @JsonProperty("name") @Input(value = Type.TEXT) String name,
			@JsonProperty("multiple") @Select(options = SubItem.SubItemOptions.class, type = SuggestType.EXTERNAL) final List<SubItem> multiple,
			@JsonProperty("type") @Select final ItemType type) {
		this.key = key;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
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
}
