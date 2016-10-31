package com.github.hateoas.forms.spring.xhtml.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.hateoas.Identifiable;

import com.github.hateoas.forms.action.Options;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestImpl;
import com.github.hateoas.forms.spring.AffordanceBuilder;
import com.github.hateoas.forms.spring.halforms.beans.DummyController.RemoteOptions;

public class SubItem implements Identifiable<Integer>, Serializable {

	public static final SubItem[] VALIDS = { new SubItem(1, "S" + 1), new SubItem(2, "S" + 2), new SubItem(3, "S" + 3),
			new SubItem(4, "S" + 4) };

	public static final SubItem INVALID_VALUE = new SubItem(Integer.MAX_VALUE, "S" + Integer.MAX_VALUE);

	public static final SubItem[][] VALIDS_SELECTED = new SubItem[VALIDS.length + 1][];

	static {
		for (int i = 0; i < VALIDS_SELECTED.length; i++) {
			List<SubItem> valid = new ArrayList<SubItem>();
			for (int j = 0; j < i; j++) {
				valid.add(VALIDS[j]);
			}
			VALIDS_SELECTED[i] = valid.toArray(new SubItem[valid.size()]);
		}
	}

	private Integer id;

	private String name;

	public SubItem() {

	}

	public SubItem(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		SubItem other = (SubItem) obj;
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

	public static class SubItemOptions implements Options<SubItem> {

		@Override
		public List<Suggest<SubItem>> get(final String[] value, final Object... args) {
			return SuggestImpl.wrap(Arrays.asList(VALIDS), null, "name");
		}

	}

	public static class SubItemOptionsId implements Options<SubItem> {

		@Override
		public List<Suggest<SubItem>> get(final String[] value, final Object... args) {
			return SuggestImpl.wrap(Arrays.asList(VALIDS), "id", "name");
		}

	}

	public static class SubItemSearchableOptions extends RemoteOptions {
		public SubItemSearchableOptions() {
			super(AffordanceBuilder.methodOn(DummyController.class).search(null), "id", "name");
		}
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
