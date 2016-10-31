package com.github.hateoas.forms.spring.xhtml.beans;

import java.io.Serializable;

import com.github.hateoas.forms.spring.AffordanceBuilder;
import com.github.hateoas.forms.spring.halforms.beans.DummyController.RemoteOptions;

public class AnotherSubItem implements Serializable {

	public static final AnotherSubItem[] VALIDS = { new AnotherSubItem("1", "S" + 1), new AnotherSubItem("2", "S" + 2),
			new AnotherSubItem("3", "S" + 3), new AnotherSubItem("4", "S" + 4) };

	public static final AnotherSubItem INVALID_VALUE = new AnotherSubItem(Integer.MAX_VALUE + "", "S" + Integer.MAX_VALUE);

	private String name;

	private String owner;

	public AnotherSubItem() {

	}

	public AnotherSubItem(final String name, final String owner) {
		this.name = name;
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public static class SubItemSearchableOptions extends RemoteOptions {
		public SubItemSearchableOptions() {
			super(AffordanceBuilder.methodOn(DummyController.class).searchAnother(null), null, "name");
		}
	}
}
