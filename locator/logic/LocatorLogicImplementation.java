package wbs.sms.locator.logic;

import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.asin;
import static java.lang.StrictMath.atan;
import static java.lang.StrictMath.atan2;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.sqrt;
import static java.lang.StrictMath.tan;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.locator.model.BiaxialEllipsoid;
import wbs.sms.locator.model.EastNorth;
import wbs.sms.locator.model.LongLat;
import wbs.sms.locator.model.MercatorProjection;

@SingletonComponent ("locatorLogic")
public
class LocatorLogicImplementation
	implements LocatorLogic {

	/**
	 * Converts longitude/latitude to easting/northing, according to a mercator
	 * projection.
	 */
	@Override
	public
	EastNorth longLatToEastNorth (
			MercatorProjection mercatorProjection,
			LongLat longLat) {

		// ellipsoid constants

		double a =
			mercatorProjection
				.getBiaxialEllipsoid ()
				.getSemiMajorAxisA ();

		double b =
			mercatorProjection
				.getBiaxialEllipsoid ()
				.getSemiMajorAxisB ();

		double e2 =
			(pow2 (a) - pow2 (b)) / pow2 (a);

		// projection constants

		double N0 =
			mercatorProjection
				.getOriginNorthing ();

		double E0 =
			mercatorProjection
				.getOriginEasting ();

		double F0 =
			mercatorProjection
				.getScaleFactor ();

		double phi0 =
			degToRad (
				mercatorProjection
					.getOriginLatitude ());

		double lambda0 =
			degToRad (
				mercatorProjection
					.getOriginLongitude ());

		// input

		double phi =
			degToRad (
				longLat.latitude ());

		double lambda =
			degToRad (
				longLat.longitude ());

		// workings 1

		double n = (a - b) / (a + b);
		double v = a * F0 * pow(1.0 - e2 * sin2(phi), -0.5);
		double rho = a * F0 * (1.0 - e2) * pow(1.0 - e2 * sin2(phi), -1.5);
		double eta2 = v / rho - 1.0;
		double M = calculateM(b, F0, n, phi, phi0);

		// workings 2

		double I = M + N0;
		double II = v / 2 * sin(phi) * cos(phi);
		double III = v / 24.0D * sin(phi) * cos3(phi)
				* (5 - tan2(phi) + 9 * eta2);
		double IIIA = v / 720 * sin(phi) * cos5(phi)
				* (61 - 58 * tan2(phi) + tan4(phi));
		double IV = v * cos(phi);
		double V = v / 6 * cos3(phi) * (v / rho - tan2(phi));
		double VI = v
				/ 120
				* cos5(phi)
				* (5 - 18 * tan2(phi) + tan4(phi) + 14 * eta2 - 58 * tan2(phi)
						* eta2);

		// and get the result

		double N = I + II * pow2(lambda - lambda0) + III
				* pow4(lambda - lambda0) + IIIA * pow6(lambda - lambda0);

		double E = E0 + IV * (lambda - lambda0) + V * pow3(lambda - lambda0)
				+ VI * pow5(lambda - lambda0);

		// return

		return new EastNorth (
			E,
			N);

	}

	/**
	 * Converts easting/northing to longitude/latitude, according to a mercator
	 * projection.
	 */
	@Override
	public
	LongLat eastNorthToLongLat (
			MercatorProjection mercatorProjection,
			EastNorth eastNorth) {

		// ellipsoid constants

		double a = mercatorProjection.getBiaxialEllipsoid().getSemiMajorAxisA();
		double b = mercatorProjection.getBiaxialEllipsoid().getSemiMajorAxisB();
		double e2 = (pow2(a) - pow2(b)) / pow2(a);

		// projection constants

		double N0 = mercatorProjection.getOriginNorthing();
		double E0 = mercatorProjection.getOriginEasting();
		double F0 = mercatorProjection.getScaleFactor();
		double phi0 = degToRad(mercatorProjection.getOriginLatitude());
		double lambda0 = degToRad(mercatorProjection.getOriginLongitude());

		// input

		double E = eastNorth.getEasting();
		double N = eastNorth.getNorthing();

		// workings 1

		double phiz = (N - N0) / a / F0 + phi0;
		double n = (a - b) / (a + b);
		double v = a * F0 * pow(1.0 - e2 * sin2(phiz), -0.5);
		double rho = a * F0 * (1.0 - e2) * pow(1.0 - e2 * sin2(phiz), -1.5);
		double eta2 = v / rho - 1.0;
		double M = calculateM(b, F0, n, phiz, phi0);
		while (abs(N - N0 - M) >= 0.00001) {
			phiz = (N - N0 - M) / a / F0 + phiz;
			M = calculateM(b, F0, n, phiz, phi0);
		}

		// workings 2

		double VII = tan(phiz) / 2 / rho / v;
		double VIII = tan(phiz) / 24 / rho / pow3(v)
				* (5 + 3 * tan2(phiz) + eta2 - 9 * tan2(phiz) * eta2);

		double IX = tan(phiz) / 720 / rho / pow5(v)
				* (61 + 90 * tan2(phiz) + 45 * tan4(phiz));

		double X = 1 / cos(phiz) / v;

		double XI = 1 / cos(phiz) / 6 / pow3(v) * (v / rho + 2 * tan2(phiz));

		double XII = 1 / cos(phiz) / 120 / pow5(v)
				* (5 + 28 * tan2(phiz) + 24 * tan4(phiz));

		double XIIA = 1
				/ cos(phiz)
				/ 5040
				/ pow7(v)
				* (61 + 662 * tan2(phiz) + 1320 * tan4(phiz) + 720 * tan6(phiz));

		// and get the result

		double phi =
			+ phiz
			- VII * pow2 (E - E0)
			+ VIII * pow4(E - E0)
			- IX * pow6 (E - E0);

		double lambda =
			+ lambda0
			+ X * (E - E0)
			- XI * pow3 (E - E0)
			+ XII * pow5 (E - E0)
			- XIIA * pow7 (E - E0);

		// return

		return new LongLat (
			radToDeg (lambda),
			radToDeg (phi));

	}

	/**
	 * Calculates the distance in metres between the two longitude/latitude
	 * coordinates, according to a biaxial ellipsoid model.
	 */
	@Override
	public
	double distanceMetres (
			LongLat longLat1,
			LongLat longLat2,
			BiaxialEllipsoid biaxialEllipsoid) {

		// if they are really close just return zero as we tend to get NaN
		// otherwise

		if (

			abs (
				+ longLat1.longitude ()
				- longLat2.longitude ()
			) < dmsToDeg (0, 0, 0.0001D)

			&& abs (
				+ longLat2.latitude ()
				- longLat2.latitude ()
			) < dmsToDeg (0, 0, 0.0001D)

		) {

			return 0.0D;

		}

		// ellipsoid constants

		double a =
			biaxialEllipsoid
				.getSemiMajorAxisA ();

		double b =
			biaxialEllipsoid
				.getSemiMajorAxisB ();

		double f = (a - b) / a;

		// input

		double lat1 =
			degToRad (
				longLat1.latitude ());

		double lat2 =
			degToRad (
				longLat2.latitude ());

		double L =
			+ degToRad (
				longLat1.longitude ())
			- degToRad (
				longLat2.longitude ());

		// workings 1

		double U1 = atan((1 - f) * tan(lat1));
		double U2 = atan((1 - f) * tan(lat2));
		double sinU1 = sin(U1), cosU1 = cos(U1);
		double sinU2 = sin(U2), cosU2 = cos(U2);
		double lambda = L, lambdaP = 2 * PI;

		// workings 2

		int iterLimit = 20;

		double sinLambda;
		double cosLambda;
		double sinSigma;
		double cosSigma;
		double sigma;
		double alpha;
		double cosSqAlpha;
		double cos2SigmaM;
		double C;

		do {

			if (iterLimit -- == 0)
				return Double.NaN;

			sinLambda =
				sin (lambda);

			cosLambda =
				cos (lambda);

			sinSigma =
				sqrt (
					+ (1
						* cosU2
						* sinLambda
						* cosU2
						* sinLambda
					)
					+ (1
						* (
							+ (1
								* cosU1
								* sinU2
							)
							- (1
								* sinU1
								* cosU2
								* cosLambda
							)
						)
						* (
							+ (1
								* cosU1
								* sinU2
							)
							- (1
								* sinU1
								* cosU2
								* cosLambda
							)
						)
					)
				);

			cosSigma =
				+ sinU1
					* sinU2
				+ cosU1
					* cosU2
					* cosLambda;

			sigma =
				atan2 (
					sinSigma,
					cosSigma);

			alpha =
				asin (1
					* cosU1
					* cosU2
					* sinLambda
					/ sinSigma);

			cosSqAlpha =
				cos2 (alpha);

			cos2SigmaM =
				+ cosSigma
				- (1
					* 2
					* sinU1
					* sinU2
					/ cosSqAlpha
				);

			C = 1
				* f
				/ 16
				* cosSqAlpha
				* (
					+ 4
					+ (1
						* f
						* (
							+ 4
							- 3 * cosSqAlpha
						)
					)
				);

			lambdaP =
				lambda;

			lambda =
				+ L
				+ (1
					* (1 - C)
					* f
					* sin (alpha)
					* (
						+ sigma
						+ (1
							* C
							* sinSigma
							* (
								+ cos2SigmaM
								+ (1
									* C
									* cosSigma
									* (
										- 1
										+ (1
											* 2
											* cos2SigmaM
											* cos2SigmaM
										)
									)
								)
							)
						)
					)
				);

		} while (

			abs (
				+ lambda
				- lambdaP
			) >= 0.000000000001

		);

		// workings 3

		double uSq = 1
			* cosSqAlpha
			* (
				+ a * a
				- b * b
			)
			/ (
				b * b
			);

		double A =
			+ 1
			+ 1
				* uSq
				/ 16384
				* (
					+ 4096
					+ uSq * (
						- 768
						+ uSq * (
							+ 320
							- 175
								* uSq
						)
					)
				);

		double B = 1
			* uSq
			/ 1024
			* (
				+ 256
				+ uSq * (
					- 128
					+ uSq * (
						+ 74
						- 47
							* uSq
					)
				)
			);

		double deltaSigma = 1
			* B
			* sinSigma
			* (
				+ cos2SigmaM
				+ (1
					* B
					/ 4
					* (
						+ cosSigma * (
							- 1
							+ (1
								* 2
								* cos2SigmaM
								* cos2SigmaM
							)
						)
						- (1
							* B
							/ 6
							* cos2SigmaM
							* (
								- 3
								+ (1
									* 4
									* sinSigma
									* sinSigma
								)
							)
							* (
								- 3
								+ (1
									* 4
									* cos2SigmaM
									* cos2SigmaM
								)
							)
						)
					)
				)
			);

		// and return

		return 1
			* b
			* A
			* (
				+ sigma
				- deltaSigma
			);

	}

	/**
	 * Convenience function calls distanceMetres() using the standard GRS80
	 * ellipsoid.
	 */
	@Override
	public
	double distanceMetres (
			LongLat longLat1,
			LongLat longLat2) {

		return distanceMetres (
			longLat1,
			longLat2,
			BiaxialEllipsoid.grs80);

	}

	/** Convenience function calls distanceMetres() and converts to miles. */
	@Override
	public
	double distanceMiles (
			LongLat longLat1,
			LongLat longLat2,
			BiaxialEllipsoid biaxialEllipsoid) {

		return distanceMetres (
			longLat1,
			longLat2,
			biaxialEllipsoid
		) / 1609.344;

	}

	/**
	 * Convenience function calls distanceMetres() using the standard GRS80
	 * ellipsoid and converts to miles.
	 */
	@Override
	public
	double distanceMiles (
			LongLat longLat1,
			LongLat longLat2) {

		return distanceMetres (
			longLat1,
			longLat2,
			BiaxialEllipsoid.grs80
		) / 1609.344;

	}

	/** Long equation used by both conversion functions. */
	private
	double calculateM (
			double b,
			double F0,
			double n,
			double phi,
			double phi0) {

		return b
				* F0
				* (+ (1 + n + 5 / 4 * pow2 (n) + 5.0D / 4.0D * pow3 (n))
						* (phi - phi0)
						- (3 * n + 3 * pow2 (n) + 21.0D / 8.0D * pow3 (n))
						* sin (phi - phi0) * cos(phi + phi0)
						+ (15.0D / 8.0D * pow2 (n) + 15.0D / 8.0D * pow3 (n))
						* sin (2.0D * (phi - phi0)) * cos (2.0D * (phi + phi0)) - 35.0D
						/ 24.0D
						* pow3 (n)
						* sin (3.0D * (phi - phi0))
						* cos (3.0D * (phi + phi0)));
	}

	/** Converts radians to degrees. */
	@Override
	public
	double radToDeg (
			double radians) {

		return radians * 180.0D / PI;

	}

	/** Converts degrees to radians. */
	@Override
	public
	double degToRad (
			double degrees) {

		return degrees * PI / 180.0D;

	}

	/** Converts degrees, minutes and seconds to radians. */
	@Override
	public
	double dmsToRad (
			int degrees,
			int minutes,
			double seconds) {

		return degToRad (
			dmsToDeg (
				degrees,
				minutes,
				seconds));

	}

	@Override
	public
	double dmsToDeg (
			int degrees,
			int minutes,
			double seconds) {

		if (degrees < 0) {

			throw new IllegalArgumentException (
				"Degrees must be >= 0 (for negative angles negate the result)");

		}

		if (minutes < 0
				|| minutes >= 60) {

			throw new IllegalArgumentException (
				"Minutes must be >= 0 and < 60");

		}

		if (seconds < 0.0D
				|| seconds >= 60.0D) {

			throw new IllegalArgumentException (
				"Seconds must be >= 0.0D and < 60.0D");

		}

		return
			+ (double) degrees
			+ (double) minutes / 60.0D
			+ seconds / 3600.0D;

	}

	double tan2 (
			double a) {

		return pow2 (
			tan (a));

	}

	double tan4 (
			double a) {

		return pow4 (
			tan (a));

	}

	double tan6 (
			double a) {

		return pow6 (
			tan (a));

	}

	double sin2 (
			double a) {

		return pow2 (
			sin (a));

	}

	double cos2 (
			double a) {

		return pow2 (
			cos (a));

	}

	double cos3 (
			double a) {

		return pow3 (
			cos (a));

	}

	double cos5 (
			double a) {

		return pow5 (
			cos (a));

	}

	double pow2 (
			double n) {

		return (1
			* n
			* n
		);

	}

	double pow3 (
			double n) {

		return (1
			* n
			* n
			* n
		);

	}

	double pow4 (
			double n) {

		return (1
			* n
			* n
			* n
			* n
		);

	}

	double pow5 (
			double n) {

		return (1
			* n
			* n
			* n
			* n
			* n
		);

	}

	double pow6 (
			double n) {

		return (1
			* n
			* n
			* n
			* n
			* n
			* n
		);

	}

	double pow7 (
			double n) {

		return (1
			* n
			* n
			* n
			* n
			* n
			* n
			* n
		);

	}

}
