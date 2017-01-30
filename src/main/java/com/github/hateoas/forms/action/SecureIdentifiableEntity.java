package com.github.hateoas.forms.action;

import java.io.Serializable;

public interface SecureIdentifiableEntity<ID extends Serializable> extends CommonSecureIdentifiable {
	/**
	 * Returns the id identifying the object.
	 * 
	 * @return the identifier or {@literal null} if not available.
	 */
	ID getEntityId();

	void setEntityId(ID id);
}
