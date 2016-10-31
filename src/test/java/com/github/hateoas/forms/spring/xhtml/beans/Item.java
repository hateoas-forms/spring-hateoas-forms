package com.github.hateoas.forms.spring.xhtml.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.github.hateoas.forms.action.DTOParam;
import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Options;
import com.github.hateoas.forms.action.Select;
import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.SimpleSuggest;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestObjectWrapper;
import com.github.hateoas.forms.affordance.SuggestType;

public class Item implements Serializable {

	private int id;

	private String name;

	private ItemType type;

	private List<SubItem> multiple;

	private SubItem singleSub;

	private int searchedSubItem;

	private int subItemId;

	private double amount;

	private AnotherSubItem another;

	private SubEntity subEntity;

	private List<ListableSubEntity> listSubEntity;

	private boolean flag;

	private List<Integer> integerList;

	private List<String> undefinedList;

	private List<ListableSubEntity> wildCardEntityList;

	private List<WildCardedListableSubEntity> doubleLevelWildCardEntityList;

	private String[] stringArray;

	private ListableSubEntity[] arraySubEntity;

	private ListableSubEntity[] wildcardArraySubEntity;

	public Item(final int id, final String name) {
		this(id, name, ItemType.ONE, Collections.<SubItem> emptyList(), SubItem.VALIDS[0], SubItem.VALIDS[0].getId(),
				SubItem.VALIDS[0].getId(), AnotherSubItem.VALIDS[0], null, null, 1.0, false, Collections.<Integer> emptyList(),
				Collections.<String> emptyList(), Collections.<ListableSubEntity> emptyList(),
				Collections.<WildCardedListableSubEntity> emptyList(), new String[1], new ListableSubEntity[1],
				new ListableSubEntity[1]);
	}

	@JsonCreator
	public Item(@JsonProperty("id") @Input(value = Type.NUMBER) final int id,
			final @JsonProperty("name") @Input(value = Type.TEXT) String name,
			@JsonProperty("type") @Select final ItemType type,
			@JsonProperty("multiple") @Select(options = SubItem.SubItemOptions.class,
					type = SuggestType.EXTERNAL) final List<SubItem> multiple,
			@JsonProperty("singleSub") @Select(options = SubItem.SubItemOptions.class,
					type = SuggestType.EXTERNAL) final SubItem singleSub,
			@JsonProperty("subItemId") @Select(options = SubItem.SubItemOptionsId.class,
					type = SuggestType.EXTERNAL) final int subItemId,
			@JsonProperty("searchedSubItem") @Select(options = SubItem.SubItemSearchableOptions.class,
					type = SuggestType.REMOTE) final int searchedSubItem,
			@JsonProperty("another") @Select(options = AnotherSubItem.SubItemSearchableOptions.class,
					type = SuggestType.REMOTE) final AnotherSubItem another,
			@JsonProperty("subEntity") final SubEntity subEntity,
			@JsonProperty("listSubEntity") @DTOParam(wildcard = false) final List<ListableSubEntity> listSubEntity,
			@JsonProperty("amount") @Input(value = Type.NUMBER) final double amount,
			@JsonProperty("flag") @Select(value = { "true", "false" }) final boolean flag,
			@JsonProperty("integerList") @Select(options = IntegerListOptions.class) final List<Integer> integerList,
			@JsonProperty("undefinedList") @Input(maxLength = 10) final List<String> undefinedList,
			@JsonProperty("wildCardEntityList") @DTOParam(wildcard = true) final List<ListableSubEntity> wildCardEntityList,
			@JsonProperty("doubleLevelWildCardEntityList") @DTOParam(
					wildcard = true) final List<WildCardedListableSubEntity> doubleLevelWildCardEntityList,
			@JsonProperty("stringArray") @Input(value = Type.TEXT) final String[] stringArray,
			@JsonProperty("arraySubEntity") @DTOParam final ListableSubEntity[] arraySubEntity,
			@JsonProperty("wildcardArraySubEntity") @DTOParam(
					wildcard = true) final ListableSubEntity[] wildcardArraySubEntity) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.multiple = multiple;
		this.singleSub = singleSub;
		this.subItemId = subItemId;
		this.searchedSubItem = searchedSubItem;
		this.amount = amount;
		this.another = another;
		this.subEntity = subEntity;
		this.listSubEntity = listSubEntity;
		this.flag = flag;
		this.integerList = integerList;
		this.wildCardEntityList = wildCardEntityList;
		this.doubleLevelWildCardEntityList = doubleLevelWildCardEntityList;
		this.stringArray = stringArray;
		this.arraySubEntity = arraySubEntity;
		this.wildcardArraySubEntity = wildcardArraySubEntity;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ItemType getType() {
		return type;
	}

	public List<SubItem> getMultiple() {
		return multiple;
	}

	public void setMultiple(final List<SubItem> multiple) {
		this.multiple = multiple;
	}

	public SubItem getSingleSub() {
		return singleSub;
	}

	public int getSubItemId() {
		return subItemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Item other = (Item) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setType(final ItemType type) {
		this.type = type;
	}

	public void setSubItemId(final int subItemId) {
		this.subItemId = subItemId;
	}

	public int getSearchedSubItem() {
		return searchedSubItem;
	}

	public double getAmount() {
		return amount;
	}

	public void setSingleSub(final SubItem singleSub) {
		this.singleSub = singleSub;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}

	public void setSearchedSubItem(final int searchedSubItem) {
		this.searchedSubItem = searchedSubItem;
	}

	public AnotherSubItem getAnother() {
		return another;
	}

	public void setAnother(final AnotherSubItem another) {
		this.another = another;
	}

	public SubEntity getSubEntity() {
		return subEntity;
	}

	public void setSubEntity(final SubEntity subEntity) {
		this.subEntity = subEntity;
	}

	public List<ListableSubEntity> getListSubEntity() {
		return listSubEntity;
	}

	public void setListSubEntity(final List<ListableSubEntity> listSubEntity) {
		this.listSubEntity = listSubEntity;
	}

	public static class IntegerListOptions implements Options<SuggestObjectWrapper<Integer>> {

		@Override
		public List<Suggest<SuggestObjectWrapper<Integer>>> get(final String[] value, final Object... args) {
			return SimpleSuggest.wrap(new Integer[] { 0, 1, 2, 3, 4, 5 });
		}

	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(final boolean flag) {
		this.flag = flag;
	}

	public List<Integer> getIntegerList() {
		return integerList;
	}

	public void setIntegerList(final List<Integer> integerList) {
		this.integerList = integerList;
	}

	public List<String> getUndefinedList() {
		return undefinedList;
	}

	public void setUndefinedList(final List<String> undefinedList) {
		this.undefinedList = undefinedList;
	}

	public List<ListableSubEntity> getWildCardEntityList() {
		return wildCardEntityList;
	}

	public void setWildCardEntityList(final List<ListableSubEntity> wildCardEntityList) {
		this.wildCardEntityList = wildCardEntityList;
	}

	public List<WildCardedListableSubEntity> getDoubleLevelWildCardEntityList() {
		return doubleLevelWildCardEntityList;
	}

	public void setDoubleLevelWildCardEntityList(final List<WildCardedListableSubEntity> doubleLevelWildCardEntityList) {
		this.doubleLevelWildCardEntityList = doubleLevelWildCardEntityList;
	}

	public String[] getStringArray() {
		return stringArray;
	}

	public void setStringArray(final String[] stringArray) {
		this.stringArray = stringArray;
	}

	public ListableSubEntity[] getArraySubEntity() {
		return arraySubEntity;
	}

	public void setArraySubEntity(final ListableSubEntity[] arraySubEntity) {
		this.arraySubEntity = arraySubEntity;
	}

	public ListableSubEntity[] getWildcardArraySubEntity() {
		return wildcardArraySubEntity;
	}

	public void setWildcardArraySubEntity(final ListableSubEntity[] wildcardArraySubEntity) {
		this.wildcardArraySubEntity = wildcardArraySubEntity;
	}
}
