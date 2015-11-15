package wbs.sms.locator.model;

public
interface BiaxialEllipsoid {

	Double getSemiMajorAxisA ();

	Double getSemiMajorAxisB ();

	public final static
	BiaxialEllipsoidImplementation airy1830 =
		new BiaxialEllipsoidImplementation (
			6377563.396D,
			6356256.91D);

	public final static
	BiaxialEllipsoidImplementation airy1830modified =
		new BiaxialEllipsoidImplementation (
			6377340.189D,
			6356034.447D);

	public final static
	BiaxialEllipsoidImplementation grs80 =
		new BiaxialEllipsoidImplementation (
			6378137.0D,
			6356752.3141D);

}
