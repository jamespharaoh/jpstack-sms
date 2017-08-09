package wbs.sms.locator.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentSafe;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.NonNull;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
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

		return new String [] {
			"longitude",
			"latitude"
		};

	}

	@Override
	public
	Type[] getPropertyTypes () {

		return new Type [] {
			StandardBasicTypes.DOUBLE,
			StandardBasicTypes.DOUBLE
		};

	}

	@Override
	public
	Object getPropertyValue (
			@NonNull Object component,
			int property)
		throws HibernateException {

		LongLat longLat =
			(LongLat) component;

		switch (property) {

		case 0:
			return longLat.longitude ();

		case 1:
			return longLat.latitude ();

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

		return optionalEqualOrNotPresentSafe (
			optionalFromNullable (
				leftObject),
			optionalFromNullable (
				rightObject));

	}

	@Override
	public
	int hashCode (
			@NonNull Object value)
		throws HibernateException {

		return value.hashCode ();

	}

	@Override
	public
	Object nullSafeGet (
			@NonNull ResultSet resultSet,
			@NonNull String[] names,
			@NonNull SharedSessionContractImplementor session,
			@NonNull Object owner)
		throws
			HibernateException,
			SQLException {

		Double longitude =
			(Double)
			StandardBasicTypes.DOUBLE.nullSafeGet (
				resultSet,
				names [0],
				session);

		Double latitude =
			(Double)
			StandardBasicTypes.DOUBLE.nullSafeGet (
				resultSet,
				names [1],
				session);

		if (
			longitude == null
			&& latitude == null
		) {

			return null;

		}

		if (
			longitude == null
			|| latitude == null
		) {

			throw new RuntimeException ();

		}

		return new LongLat (
			longitude,
			latitude);

	}

	@Override
	public
	void nullSafeSet (
			@NonNull PreparedStatement statement,
			Object value,
			int index,
			@NonNull SharedSessionContractImplementor session)
		throws
			HibernateException,
			SQLException {

		if (value == null) {

			StandardBasicTypes.DOUBLE.nullSafeSet (
				statement,
				null,
				index,
				session);

			StandardBasicTypes.DOUBLE.nullSafeSet (
				statement,
				null,
				index + 1,
				session);

		} else {

			LongLat longLat =
				(LongLat) value;

			StandardBasicTypes.DOUBLE.nullSafeSet (
				statement,
				longLat.longitude (),
				index,
				session);

			StandardBasicTypes.DOUBLE.nullSafeSet (
				statement,
				longLat.latitude (),
				index + 1,
				session);

		}

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
			@NonNull Object value,
			@NonNull SharedSessionContractImplementor session)
		throws HibernateException {

		return (Serializable) value;

	}

	@Override
	public
	Object assemble (
			@NonNull Serializable cached,
			@NonNull SharedSessionContractImplementor session,
			@NonNull Object object)
		throws HibernateException {

		return cached;

	}

	@Override
	public
	Object replace (
			@NonNull Object original,
			@NonNull Object target,
			@NonNull SharedSessionContractImplementor session,
			@NonNull Object owner)
		throws HibernateException {

		return original;

	}

}
