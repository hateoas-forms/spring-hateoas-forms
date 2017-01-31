package com.github.hateoas.forms.action;

import java.io.Serializable;

import org.springframework.hateoas.Identifiable;

public interface SecureIdentifiable<ID extends Serializable> extends Identifiable<ID>, SecureIdContainer {

	void setId(ID id);

}
