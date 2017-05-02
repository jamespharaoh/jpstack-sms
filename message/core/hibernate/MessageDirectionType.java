package wbs.sms.message.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;

import wbs.sms.message.core.model.MessageDirection;

public
class MessageDirectionType
	extends EnumUserType<Integer,MessageDirection> {

	{

		sqlType (Types.INTEGER);
		enumClass (MessageDirection.class);

		add (0, MessageDirection.in);
		add (1, MessageDirection.out);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new MessageDirectionType ());

}
