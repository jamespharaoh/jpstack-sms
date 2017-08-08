package wbs.web.exceptions;

import lombok.Getter;

public
class InternalRedirectException
	extends RuntimeException {

	private static final
	long serialVersionUID =
		8963415525414232322L;

	@Getter
	String location;

	public
	InternalRedirectException (
			String newLocation) {

		super (
			newLocation);

		location =
			newLocation;

	}

}
