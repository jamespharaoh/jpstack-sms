package wbs.sms.locator.model;

public final
class BiaxialEllipsoidImpl
	implements BiaxialEllipsoid {

	private final
	double semiMajorAxisA;

	private final
	double semiMajorAxisB;

	public
	BiaxialEllipsoidImpl (
			double newSemiMajorAxisA,
			double newSemiMajorAxisB) {

		semiMajorAxisA =
			newSemiMajorAxisA;

		semiMajorAxisB =
			newSemiMajorAxisB;

	}

	@Override
	public
	Double getSemiMajorAxisA () {

		return semiMajorAxisA;

	}

	@Override
	public
	Double getSemiMajorAxisB () {

		return semiMajorAxisB;

	}

}
