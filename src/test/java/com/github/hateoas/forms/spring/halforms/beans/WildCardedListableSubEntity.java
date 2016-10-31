package com.github.hateoas.forms.spring.halforms.beans;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.SuggestType;

public class WildCardedListableSubEntity implements Serializable {
	private int lkey;

	private String lname;

	private ListableItemType type;

	private List<ListableItemType> multiple;

	private List<SubItem> subItemList;

	@JsonCreator
	public WildCardedListableSubEntity(@JsonProperty("lkey") @Input(value = Type.NUMBER) final int lkey,
			final @JsonProperty("lname") @Input(value = Type.TEXT) String lname, @JsonProperty("type") @Select final ListableItemType ltype,
			@JsonProperty("multiple") @Select(type = SuggestType.EXTERNAL) final List<ListableItemType> multiple,
			@JsonProperty("subItemList") @DTOParam(wildcard = true) final List<SubItem> subItemList) {
		this.lkey = lkey;
		this.lname = lname;
		type = ltype;
		this.multiple = multiple;
		this.subItemList = subItemList;
	}

	public int getLkey() {
		return lkey;
	}

	public void setLkey(final int lkey) {
		this.lkey = lkey;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(final String lname) {
		this.lname = lname;
	}

	public ListableItemType getType() {
		return type;
	}

	public void setType(final ListableItemType type) {
		this.type = type;
	}

	public List<ListableItemType> getMultiple() {
		return multiple;
	}

	public void setMultiple(final List<ListableItemType> multiple) {
		this.multiple = multiple;
	}

	public List<SubItem> getSubItemList() {
		return subItemList;
	}

	public void setSubItemList(final List<SubItem> subItemList) {
		this.subItemList = subItemList;
	}

}
