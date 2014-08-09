package wbs.sms.locator.model;

import lombok.Getter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final
class EastNorth {

	@Getter
	private final
	double easting;

	@Getter
	private final
	double northing;

	public
	EastNorth (
			double newEasting,
			double newNorthing) {

		easting =
			newEasting;

		northing =
			newNorthing;

	}

	@Override
	public
	int hashCode () {

		return new HashCodeBuilder ()
			.append (easting)
			.append (northing)
			.toHashCode ();

	}

	@Override
	public
	boolean equals (
			Object object) {

		if (this == object)
			return true;

		if (! (object instanceof EastNorth))
			return false;

		EastNorth other =
			(EastNorth) object;

		return easting == other.easting
				&& northing == other.northing;

	}

	@Override
	public
	String toString () {

		return String.format (
			"EastNorth (%f E, %f N)",
			easting,
			northing);

	}

}
