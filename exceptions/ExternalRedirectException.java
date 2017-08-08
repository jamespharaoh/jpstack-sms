package wbs.web.exceptions;

import lombok.Getter;

public
class ExternalRedirectException
	extends RuntimeException {

	@Getter
	String location;

	public
	ExternalRedirectException (
			String newLocation) {

		super (
			newLocation);

		location =
			newLocation;

	}

}
