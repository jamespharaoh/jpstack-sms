package wbs.sms.message.report.logic;

import org.joda.time.ReadableInstant;

import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.route.core.model.RouteRec;

public
interface ReportLogic {

	void deliveryReport (
			MessageRec message,
			MessageStatus newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	MessageRec deliveryReport (
			RouteRec route,
			String otherId,
			MessageStatus newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	void deliveryReport (
			int messageId,
			MessageStatus newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

}
