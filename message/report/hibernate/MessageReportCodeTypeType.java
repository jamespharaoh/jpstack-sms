package wbs.sms.message.report.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;
import wbs.sms.message.report.model.MessageReportCodeType;

public
class MessageReportCodeTypeType
	extends EnumUserType<Integer,MessageReportCodeType> {

	{

		sqlType (Types.INTEGER);
		enumClass (MessageReportCodeType.class);

		add (1, MessageReportCodeType.mig);
		add (2, MessageReportCodeType.dialogue);
		add (3, MessageReportCodeType.hybyte);
		add (4, MessageReportCodeType.mediaburst);
		add (5, MessageReportCodeType.unwiredPlaza);
		add (6, MessageReportCodeType.comshen);
		add (7, MessageReportCodeType.broadcastSystems);
		add (8, MessageReportCodeType.oxygen8);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new MessageReportCodeTypeType ());

}