package wbs.sms.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;

import wbs.sms.core.model.SmsOrigType;

public
class SmsOrigTypeHibernateType
	extends EnumUserType<Integer,SmsOrigType> {

	{

		sqlType (Types.INTEGER);
		enumClass (SmsOrigType.class);

		add (1, SmsOrigType.longNumber);
		add (2, SmsOrigType.shortCode);
		add (3, SmsOrigType.textual);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new SmsOrigTypeHibernateType ());

}
