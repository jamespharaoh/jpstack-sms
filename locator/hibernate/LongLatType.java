package wbs.sms.locator.hibernate;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import wbs.sms.locator.model.LongLat;

public
class LongLatType
	implements CompositeUserType {

	@Override
	public
	String[] getPropertyNames () {

		return new String[] {
			"longitude",
			"latitude"
		};

	}

	@Override
	public
	Type[] getPropertyTypes () {

		return new Type[] {
			StandardBasicTypes.DOUBLE,
			StandardBasicTypes.DOUBLE
		};

	}

	@Override
	public
	Object getPropertyValue (
			Object component,
			int property)
		throws HibernateException {

		LongLat longLat =
			(LongLat) component;

		switch (property) {

		case 0:
			return longLat.getLongitude ();

		case 1:
			return longLat.getLatitude ();

		}

		throw new HibernateException (
			"Invalid property " + property);

	}

	@Override
	public
	void setPropertyValue (
			Object component,
			int property,
			Object value)
		throws HibernateException {

		throw new HibernateException (
			"Immutable");

	}

	@Override
	public
	Class<?> returnedClass () {
		return LongLat.class;
	}

	@Override
	public
	boolean equals (
			Object leftObject,
			Object rightObject)
		throws HibernateException {

		if (leftObject == rightObject)
			return true;

		if (leftObject == null
				|| rightObject == null)
			return false;

		LongLat leftLongLat =
			(LongLat) leftObject;

		LongLat rightLongLat =
			(LongLat) rightObject;

		return
			equal (
				leftLongLat.getLongitude (),
				rightLongLat.getLongitude ())
			&& equal (
				leftLongLat.getLatitude (),
				rightLongLat.getLatitude ());

	}

	/*
	@Override
	public boolean equals1 (Object x, Object y)
		throws HibernateException {

		if (x == y)
			return true;

		if (x == null || y == null)
			return false;

		LongLat
			lx = (LongLat) x,
			ly = (LongLat) y;

		return
			eq (
				lx.getLongitude (),
				ly.getLongitude ())
			&& eq (
				lx.getLatitude (),
				ly.getLatitude ());
	}
	*/

	@Override
	public
	int hashCode (
			Object value)
		throws HibernateException {

		return value.hashCode ();

	}

	@Override
	public
	Object nullSafeGet (
			ResultSet result,
			String[] names,
			SessionImplementor session,
			Object owner)
		throws HibernateException,
			SQLException {

		Double longitude = (Double)
			StandardBasicTypes.DOUBLE.nullSafeGet (
				result,
				names [0],
				session);

		Double latitude = (Double)
			StandardBasicTypes.DOUBLE.nullSafeGet (
				result,
				names [1],
				session);

		if (longitude == null || latitude == null)
			return null;

		return new LongLat (longitude, latitude);
	}

	@Override
	public
	void nullSafeSet (
			PreparedStatement statement,
			Object value,
			int index,
			SessionImplementor session)
		throws HibernateException,
			SQLException {

		LongLat longLat = (LongLat) value;

		Double longitude =
			longLat != null
				? longLat.getLongitude ()
				: null;

		Double latitude =
			longLat != null
				? longLat.getLatitude ()
				: null;

		StandardBasicTypes.DOUBLE.nullSafeSet (
			statement,
			longitude,
			index,
			session);

		StandardBasicTypes.DOUBLE.nullSafeSet (
			statement,
			latitude,
			index + 1,
			session);
	}

	@Override
	public
	Object deepCopy (
			Object value)
		throws HibernateException {

		return value;

	}

	@Override
	public
	boolean isMutable () {
		return false;
	}

	@Override
	public
	Serializable disassemble (
			Object value,
			SessionImplementor session)
		throws HibernateException {

		return (Serializable) value;

	}

	@Override
	public
	Object assemble (
			Serializable cached,
			SessionImplementor session,
			Object object)
		throws HibernateException {

		return cached;

	}

	@Override
	public
	Object replace (
			Object original,
			Object target,
			SessionImplementor session,
			Object owner)
		throws HibernateException {

		return original;
	}

}
