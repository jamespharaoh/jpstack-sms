package wbs.sms.locator.model;

public
interface BiaxialEllipsoid {

	Double getSemiMajorAxisA ();

	Double getSemiMajorAxisB ();

	public final static
	BiaxialEllipsoidImpl airy1830 =
		new BiaxialEllipsoidImpl (
			6377563.396D,
			6356256.91D);

	public final static
	BiaxialEllipsoidImpl airy1830modified =
		new BiaxialEllipsoidImpl (
			6377340.189D,
			6356034.447D);

	public final static
	BiaxialEllipsoidImpl grs80 =
		new BiaxialEllipsoidImpl (
			6378137.0D,
			6356752.3141D);

}
