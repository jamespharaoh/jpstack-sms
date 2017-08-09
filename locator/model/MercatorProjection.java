package wbs.sms.locator.model;

public
interface MercatorProjection {

	BiaxialEllipsoid getBiaxialEllipsoid ();

	Double getScaleFactor ();
	Double getOriginLongitude ();
	Double getOriginLatitude ();
	Double getOriginEasting ();
	Double getOriginNorthing ();

}
